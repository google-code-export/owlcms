package com.vaadin.data.hbnutil;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.service.ApplicationContext;
import com.vaadin.service.ApplicationContext.TransactionListener;

/**
 * Example Container to use with Hibernate.
 * 
 * Lazy, almost full-featured, general purpose Hibernate entity container. Makes lots of queries, but shouldn't consume too much memory.
 * 
 * HbnContainer expects to be used with session-per-request pattern. In in its constructor it will only need entity class type (Pojo) and a
 * HbnSessionManager that it will use to get a hibernate sesion with open transaction.
 * 
 * HbnContainer also expects that identifiers are auto generated.
 * 
 * Note, container caches size, firstId, lastId to be much faster with large datasets.
 * 
 * VAADIN_TODO make this caching optional, actually should trust on Hibernates and DB engines query caches.
 * 
 * VAADIN_TODO Better documentation
 * 
 * @author Matti Tahvonen (IT Mill)
 * @author Henri Sara (IT Mill)
 * @author Daniel Bell (itree.com.au, bugfixes, support for embedded composite keys, ability to add non Hibernate-mapped properties)
 * @author jflamy (use of setters, integration of search criteria as suggested in forum, some type safety, use of valueOf() for dealing with
 *         enumerated types, clearCache, allow linking to a null object to hide association)
 */
public class HbnContainer<T> implements Container.Indexed, Container.Sortable, Container.ItemSetChangeNotifier,
        Container.Filterable {

    private static final long serialVersionUID = -4265365834988905889L;

    public interface HbnSessionManager {
        /**
         * @return a Hibernate Session with open transaction
         */
        Session getHbnSession();
    }

    /**
     * Item wrappping an entity class.
     */
    public class EntityItem<ET> implements Item {
        private static final long serialVersionUID = -2847179724504965599L;

        protected Object pojo;

        protected Map<Object, Property> properties = new HashMap<Object, Property>();

        public EntityItem(Serializable id) {
            pojo = hbnSessionManager.getHbnSession().get(type, id);

            // add non-hibernate mapped container properties
            // (the hibernate-managed properties are created as
            // EntityItemProperty)
            for (String propertyId : addedProperties.keySet()) {
                addItemProperty(propertyId, new MethodProperty<Object>(pojo, propertyId));
            }
        }

        public Object getPojo() {
            return pojo;
        }

        @Override
        public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
            properties.put(id, property);
            return true;
        }

        @Override
        public Property getItemProperty(Object id) {
            Property p = properties.get(id);
            if (p == null) {
                p = new EntityItemProperty(id.toString());
                properties.put(id, p);
            }
            return p;
        }

        @Override
        public Collection<String> getItemPropertyIds() {
            return getContainerPropertyIds();
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            Property removed = properties.remove(id);
            return removed != null;
        }

        public class EntityItemProperty implements Property, Property.ValueChangeNotifier {

            private static final long serialVersionUID = 9137465427518038567L;
            private String propertyName;

            public EntityItemProperty(String propertyName) {
                this.propertyName = propertyName;
            }

            private Type getPropertyType() {
                return getClassMetadata().getPropertyType(propertyName);
            }

            private boolean propertyInEmbeddedKey() {
                // VAADIN_VAADIN_TODO a place for optimization
                Type idType = getClassMetadata().getIdentifierType();
                if (idType.isComponentType()) {
                    ComponentType idComponent = (ComponentType) idType;
                    String[] idPropertyNames = idComponent.getPropertyNames();
                    List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
                    if (idPropertyNameList.contains(propertyName)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public Class<?> getType() {
                // VAADIN_TODO clean, optimize, review

                if (propertyInEmbeddedKey()) {
                    ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
                    String[] propertyNames = idType.getPropertyNames();
                    for (int i = 0; i < propertyNames.length; i++) {
                        String name = propertyNames[i];
                        if (name.equals(propertyName)) {
                            try {
                                String idName = getClassMetadata().getIdentifierPropertyName();
                                Field idField = type.getDeclaredField(idName);
                                Field propertyField = idField.getType().getDeclaredField(propertyName);
                                return propertyField.getType();
                            } catch (NoSuchFieldException ex) {
                                throw new RuntimeException("Could not find the type of specified container property.",
                                        ex);
                            }
                        }
                    }
                }

                Type propertyType = getPropertyType();

                if (propertyType.isCollectionType()) {
                    Class<?> returnedClass = propertyType.getReturnedClass();
                    return returnedClass;
                } else if (propertyType.isAssociationType()) {
                    // VAADIN_TODO clean, optimize, review
                    ClassMetadata classMetadata2 = hbnSessionManager.getHbnSession().getSessionFactory()
                            .getClassMetadata(getClassMetadata().getPropertyType(propertyName).getReturnedClass());
                    return classMetadata2.getIdentifierType().getReturnedClass();

                } else {
                    return getClassMetadata().getPropertyType(propertyName).getReturnedClass();
                }
            }

            @Override
            public Object getValue() {
                // VAADIN_TODO clean, optimize, review

                if (propertyInEmbeddedKey()) {
                    ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
                    String[] propertyNames = idType.getPropertyNames();
                    for (int i = 0; i < propertyNames.length; i++) {
                        String name = propertyNames[i];
                        if (name.equals(propertyName)) {
                            Object id = getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
                            return idType.getPropertyValue(id, i, EntityMode.POJO);
                        }
                    }
                }

                Type propertyType = getPropertyType();
                Object propertyValue = getClassMetadata().getPropertyValue(pojo, propertyName, EntityMode.POJO);
                if (!propertyType.isAssociationType()) {
                    return propertyValue;
                } else if (propertyType.isCollectionType()) {
                    if (propertyValue == null) {
                        return null;
                    }
                    HashSet<Serializable> identifiers = new HashSet<Serializable>();
                    Collection<?> pojos = (Collection<?>) propertyValue;
                    for (Object object : pojos) {
                        // here, object must be of an association type
                        if (!hbnSessionManager.getHbnSession().contains(object)) {
                            object = hbnSessionManager.getHbnSession().merge(object);
                        }
                        identifiers.add(hbnSessionManager.getHbnSession().getIdentifier(object));
                    }
                    return identifiers;
                } else if (propertyType.isAssociationType()) {
                    if (propertyValue == null) {
                        return null;
                    }
                    // AssociationType associationType = (AssociationType)
                    // propertyType;
                    // System.out.println("Getting metadata for association: "
                    // + propertyType.getName());
                    Class<?> propertyTypeClass = propertyType.getReturnedClass();

                    ClassMetadata classMetadata2 = hbnSessionManager.getHbnSession().getSessionFactory()
                            .getClassMetadata(propertyTypeClass);
                    // if (classMetadata2 == null) {
                    // System.out.println("metadata for association is null!");
                    // }
                    Serializable identifier = classMetadata2.getIdentifier(propertyValue, EntityMode.POJO);
                    return identifier;
                } else {
                    return propertyValue;
                }
            }

            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public void setReadOnly(boolean newStatus) {
            }

            @Override
            public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
                try {
                    Object value = null;
                    try {
                        doSetValue(newValue, value);
                        // Persist (possibly) detached pojo
                        hbnSessionManager.getHbnSession().merge(pojo);
                        fireValueChange();

                    } catch (final java.lang.Exception e) {
                        e.printStackTrace();
                        throw new Property.ConversionException(e);
                    }

                } catch (HibernateException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Set the values using the Hibernate mechanisms.
             * 
             * @param newValue
             * @param value
             * @throws SecurityException
             * @throws InstantiationException
             * @throws IllegalAccessException
             * @throws IllegalArgumentException
             * @throws InvocationTargetException
             * @throws NoSuchMethodException
             * @throws HibernateException
             * @throws NoSuchFieldException
             */
            private void doSetValue(Object newValue, Object value) throws SecurityException, InstantiationException,
                    IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
                    HibernateException, NoSuchFieldException {

                // System.err.println("doSetValue newValue="+newValue+" value="+value);
                value = computeValue(newValue, value);

                // VAADIN_TODO clean, optimize, review
                if (propertyInEmbeddedKey()) {
                    ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
                    String[] propertyNames = idType.getPropertyNames();
                    for (int i = 0; i < propertyNames.length; i++) {
                        String name = propertyNames[i];
                        if (name.equals(propertyName)) {
                            Object id = getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
                            Object[] values = idType.getPropertyValues(id, EntityMode.POJO);
                            values[i] = value;
                            // idType.setPropertyValues(id, values,
                            // EntityMode.POJO);
                            // call the setter method.
                            PropertyDescriptor pd = propertyDescriptors.get(propertyName);
                            pd.getWriteMethod().invoke(pojo, values);

                        }
                    }
                } else {
                    Type propertyType = getClassMetadata().getPropertyType(propertyName);
                    if (propertyType.isCollectionType()) {
                        // VAADIN_TODO how to fetch mapped type properly
                        Field declaredField = type.getDeclaredField(propertyName);
                        java.lang.reflect.Type genericType = declaredField.getGenericType();
                        java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType) genericType)
                                .getActualTypeArguments();

                        java.lang.reflect.Type assosiatedType = actualTypeArguments[0];
                        String typestring = assosiatedType.toString().substring(6);

                        HashSet<Object> objectset = new HashSet<Object>();

                        Collection<?> identifiers = (Collection<?>) value;
                        Session session = hbnSessionManager.getHbnSession();
                        for (Object id : identifiers) {
                            Object object = session.get(typestring, (Serializable) id);
                            objectset.add(object);
                        }

                        // call the setter method.
                        PropertyDescriptor pd = propertyDescriptors.get(propertyName);
                        pd.getWriteMethod().invoke(pojo, objectset);

                        // getClassMetadata().setPropertyValue(pojo, propertyName, objectset, EntityMode.POJO);

                    } else if (propertyType.isAssociationType()) {
                        Class<?> referencedType = getClassMetadata().getPropertyType(propertyName).getReturnedClass();

                        Object object = null;
                        if (value != null) {
                            object = hbnSessionManager.getHbnSession().get(referencedType, (Serializable) value);
                        }

                        // getClassMetadata().setPropertyValue(pojo,propertyName, object, EntityMode.POJO);
                        // call the setter method.
                        PropertyDescriptor pd = propertyDescriptors.get(propertyName);

                        if (object != null) {
                            object = hbnSessionManager.getHbnSession().merge(object);
                        }
                        pd.getWriteMethod().invoke(pojo, object);

                        // hbnSessionManager.getHbnSession().saveOrUpdate(pojo);

                    } else {
                        // call the setter method.
                        PropertyDescriptor pd = propertyDescriptors.get(propertyName);
                        if (pd == null) {
                            throw new RuntimeException("getter/setter not defined for " + propertyName);
                        }
                        Method writeMethod = pd.getWriteMethod();
                        writeMethod.invoke(pojo, value);
                        // getClassMetadata().setPropertyValue(pojo,propertyName, value, EntityMode.POJO);
                    }
                }
            }

            /**
             * @param newValue
             * @param value
             * @return
             * @throws SecurityException
             * @throws InstantiationException
             * @throws IllegalAccessException
             * @throws IllegalArgumentException
             * @throws InvocationTargetException
             * @throws NoSuchMethodException
             */
            private Object computeValue(Object newValue, Object value) throws SecurityException,
                    InstantiationException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, NoSuchMethodException {
                // System.err.println("before newValue case1: "+getType()+" "+newValue+" "+newValue.getClass());
                if (newValue == null || getType().isAssignableFrom(newValue.getClass())) {
                    value = newValue;
                    // System.err.println("after newValue case1: "+getType()+" "+newValue+" "+newValue.getClass());
                } else {
                    Constructor<?> constr = null;
                    try {
                        // System.err.println("before newValue case2: "+getType()+" "+newValue+" "+newValue.getClass());
                        // Gets the string constructor
                        constr = getType().getConstructor(new Class[] { String.class });

                        value = constr.newInstance(new Object[] { newValue.toString() });
                    } catch (NoSuchMethodException nsme) {
                        try {
                            // System.err.println("before newValue case3: "+getType()+" "+newValue+" "+newValue.getClass());
                            // try with valueOf.
                            Method method = getType().getMethod("valueOf", String.class);
                            value = method.invoke(null, newValue.toString());
                        } catch (Exception e) {
                            throw nsme;
                        }
                    } catch (Throwable ite) {
                        // System.err.println("Throwable "+ite.getClass()+" "+newValue.getClass());
                        // VAADIN_TODO: this is a crude patch for empty values passed in for a double.
                        if (getType().isAssignableFrom(Double.class) && newValue.toString().isEmpty()) {
                            value = constr.newInstance(new Object[] { "0" });
                            System.err.println("value " + value);
                        } else {
                            // System.err.println("not a number "+ite.getClass()+" "+newValue.getClass());
                        }
                    }
                }
                return value;
            }

            @Override
            public String toString() {
                Object v = getValue();
                if (v != null) {
                    return v.toString();
                } else {
                    return null;
                }
            }

            private class HbnPropertyValueChangeEvent implements Property.ValueChangeEvent {
                private static final long serialVersionUID = 166764621324404579L;

                @Override
                public Property getProperty() {
                    return EntityItemProperty.this;
                }
            }

            private List<ValueChangeListener> valueChangeListeners;

            private void fireValueChange() {
                // System.err.println("firing from property "+System.identityHashCode(EntityItemProperty.this)+" of container "+System.identityHashCode(HbnContainer.this));
                if (valueChangeListeners != null) {
                    HbnPropertyValueChangeEvent event = new HbnPropertyValueChangeEvent();
                    Object[] array = valueChangeListeners.toArray();
                    for (int i = 0; i < array.length; i++) {
                        // System.err.println("     firing at "+System.identityHashCode(((ValueChangeListener)
                        // array[i])));
                        ((ValueChangeListener) array[i]).valueChange(event);
                    }
                }
            }

            @Override
            public void addListener(ValueChangeListener listener) {
                // System.err.println("adding listener "+System.identityHashCode(listener)+" to property "+System.identityHashCode(this)+" of container "+System.identityHashCode(HbnContainer.this)+" value="+getValue());
                if (valueChangeListeners == null) {
                    valueChangeListeners = new LinkedList<ValueChangeListener>();
                }
                if (!valueChangeListeners.contains(listener)) {
                    valueChangeListeners.add(listener);
                }
            }

            @Override
            public void removeListener(ValueChangeListener listener) {
                if (valueChangeListeners != null) {
                    valueChangeListeners.remove(listener);
                }
            }

        }
    }

    private static final int ROW_BUF_SIZE = 100;
    private static final int ID_TO_INDEX_MAX_SIZE = 300;

    /** Entity class that will be listed in container */
    protected Class<?> type;
    protected final HbnSessionManager hbnSessionManager;
    private ClassMetadata classMetadata;

    /** internal flag used to temporarely invert order of listing */
    private boolean temporaryFlippedAsc = true;

    private List<?> ascRowBuffer;
    private List<?> descRowBuffer;
    private Object lastId;
    private Object firstId;
    private int indexRowBufferFirstIndex;
    private List<?> indexRowBuffer;
    private Map<Object, Integer> idToIndex = new LinkedHashMap<Object, Integer>();
    private boolean[] orderAscendings;
    private Object[] orderPropertyIds;
    private Integer size;
    private LinkedList<ItemSetChangeListener> itemSetChangeListeners;
    private HashSet<ContainerFilter> filters;

    /** A map of added javabean property names to their respective types */
    private Map<String, Class<?>> addedProperties = new HashMap<String, Class<?>>();

    /** map of property descriptors */
    transient LinkedHashMap<String, PropertyDescriptor> propertyDescriptors;

    /**
     * Creates a new instance of HbnContainer, listing all object of given type.
     * 
     * @param entityType
     *            Entity class to be listed in container.
     * @param sessionMgr
     *            interface via Hibernate session is fetched
     */
    public HbnContainer(Class<T> entityType, HbnSessionManager sessionMgr) {
        type = entityType;
        hbnSessionManager = sessionMgr;
        propertyDescriptors = getPropertyDescriptors(entityType);
        attachVaadinTransactionListener();
    }

    /**
     * HbnContainer automatically adds all fields that are mapped by Hibernate to DB. With this method one can add a javabean property to
     * the container that is contained on pojo but not hibernate-mapped.
     * 
     * @see Container#addContainerProperty(Object, Class, Object)
     */
    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type1, Object defaultValue)
            throws UnsupportedOperationException {
        boolean propertyExists = true;
        try {
            new MethodProperty<Object>(this.type.newInstance(), propertyId.toString());
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            propertyExists = false;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            propertyExists = false;
        }
        addedProperties.put(propertyId.toString(), type1);
        return propertyExists;
    }

    /**
     * HbnContainer specific method to persist newly created entity.
     * 
     * @param entity
     *            the unsaved entity object
     * @return the identifier for newly persisted entity
     */
    public Serializable saveEntity(T entity) {
        // insert into DB
        hbnSessionManager.getHbnSession().save(entity);
        clearInternalCache();
        fireItemSetChange();
        return (Serializable) getIdForPojo(entity);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        Object o;
        try {
            o = type.newInstance();
            // insert into DB
            hbnSessionManager.getHbnSession().save(o);
            clearInternalCache();
            fireItemSetChange();
            return getIdForPojo(o);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        // Expecting autogenerated identifiers
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsId(Object itemId) {
        // test if entity can be found with given id
        try {
            return (hbnSessionManager.getHbnSession().get(type, (Serializable) itemId) != null);
        } catch (Exception e) {
            // this should not happen if used correctly
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        final EntityItem<T> item = getItem(itemId);
        if (item == null)
            return null;
        return item.getItemProperty(propertyId);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        Collection<String> propertyIds = getSortableContainerPropertyIds();
        propertyIds.addAll(addedProperties.keySet());
        return propertyIds;
    }

    private Collection<String> getEmbeddedKeyPropertyIds() {
        ArrayList<String> embeddedKeyPropertyIds = new ArrayList<String>();
        Type identifierType = getClassMetadata().getIdentifierType();
        if (identifierType.isComponentType()) {
            ComponentType idComponent = (ComponentType) identifierType;
            String[] propertyNameArray = idComponent.getPropertyNames();
            if (propertyNameArray != null) {
                List<String> propertyNames = Arrays.asList(propertyNameArray);
                embeddedKeyPropertyIds.addAll(propertyNames);
            }
        }
        return embeddedKeyPropertyIds;
    }

    /**
     * @return Hibernates ClassMetadata for the listed entity type
     */
    private ClassMetadata getClassMetadata() {
        if (classMetadata == null) {
            classMetadata = hbnSessionManager.getHbnSession().getSessionFactory().getClassMetadata(type);
        }
        return classMetadata;
    }

    @Override
    public EntityItem<T> getItem(Object itemId) {
        EntityItem<T> item = null;
        if (itemId != null) {
            item = loadItem((Serializable) itemId);
        }
        return item;
    }

    /**
     * This method is used to fetch Items by id. Override this if you need customized EntityItems.
     * 
     * @param itemId
     * @return
     */
    protected EntityItem<T> loadItem(Serializable itemId) {
        EntityItem<T> newItem = new EntityItem<T>(itemId);
        if (newItem.getPojo() == null)
            return null;
        return newItem;
    }

    boolean filteredItemIds = true;

    /**
     * Determine whether getItemIds returns all items or not. For the purpose of creating Selects based on collections with a large number
     * of items, it is useful to filter down on criteria that are not shown in the drop down. This is a workaround because AbstractSelect
     * calls getItemIds.
     * 
     * @param filteredItemIds
     *            if true, getItemIds will return only the visible elements of the container (after applicaiton of the filteringCriteria)
     */
    public void setFilteredGetItemIds(boolean filteredItemIds) {
        this.filteredItemIds = filteredItemIds;
    }

    @Override
    public Collection<?> getItemIds() {
        if (filteredItemIds) {
            return getFilteredItemIds();
        } else {
            return getAllItemIds();
        }
    }

    public Collection<?> getAllItemIds() {
        List<?> list = hbnSessionManager.getHbnSession().createQuery(
                "select " + getClassMetadata().getIdentifierPropertyName() + " from " + getClassMetadata().getEntityName())
                .list();
        return list;
    }

    public Collection<?> getFilteredItemIds() {
        List<?> objectList = getAllPojos();
        List<Serializable> list = new ArrayList<Serializable>(objectList.size());
        for (Object curObject : objectList) {
            list.add((Serializable) getIdForPojo(curObject));
        }
        return list;
    }

    /**
     * Get all the (filtered) Java objects in the container as a collection. This is useful for creating a BeanItem container or other
     * structure from a HbnContainer.
     * 
     * @return all Java objects in the container
     */
    @SuppressWarnings("unchecked")
    public List<T> getAllPojos() {
        List<T> objectList = getCriteria().list();
        return objectList;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if (addedProperties.keySet().contains(propertyId)) {
            return addedProperties.get(propertyId);
        } else if (propertyInEmbeddedKey(propertyId)) {
            ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
            String[] propertyNames = idType.getPropertyNames();
            for (int i = 0; i < propertyNames.length; i++) {
                String name = propertyNames[i];
                if (name.equals(propertyId)) {
                    String idName = getClassMetadata().getIdentifierPropertyName();
                    try {
                        Field idField = type.getDeclaredField(idName);
                        Field propertyField = idField.getType().getDeclaredField((String) propertyId);
                        return propertyField.getType();
                    } catch (NoSuchFieldException ex) {
                        throw new RuntimeException("Could not find the type of specified container property.", ex);
                    }
                }
            }
        }
        Type propertyType = getClassMetadata().getPropertyType(propertyId.toString());
        return propertyType.getReturnedClass();
    }

    private boolean propertyInEmbeddedKey(Object propertyId) {
        Type idType = getClassMetadata().getIdentifierType();
        if (idType.isComponentType()) {
            ComponentType idComponent = (ComponentType) idType;
            String[] idPropertyNames = idComponent.getPropertyNames();
            List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
            if (idPropertyNameList.contains(propertyId)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("removeAllItems");
        // return false;
    }

    /* Remove a container property added with addContainerProperty() */
    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        boolean propertyExisted = false;
        Class<?> removed = addedProperties.remove(propertyId);
        if (removed != null) {
            propertyExisted = true;
        }
        return propertyExisted;
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        Object p = hbnSessionManager.getHbnSession().load(type, (Serializable) itemId);
        // hide row from db
        hbnSessionManager.getHbnSession().delete(p);
        clearInternalCache();
        fireItemSetChange();
        return true;
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
        }

    }

    private void fireItemSetChange() {
        if (itemSetChangeListeners != null) {
            final Object[] l = itemSetChangeListeners.toArray();
            final Container.ItemSetChangeEvent event = new Container.ItemSetChangeEvent() {
                private static final long serialVersionUID = -3002746333251784195L;

                @Override
                public Container getContainer() {
                    return HbnContainer.this;
                }
            };
            for (int i = 0; i < l.length; i++) {
                ((ItemSetChangeListener) l[i]).containerItemSetChange(event);
            }
        }
    }

    @Override
    public int size() {
        if (size == null) {
            size = (Integer) getBaseCriteria().setProjection(Projections.rowCount()).uniqueResult();
        }
        return size.intValue();
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
        // return null;
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
        // return null;
    }

    /**
     * Gets a base listing using current orders etc.
     * 
     * @return criteria with current Order criterias added
     */
    private Criteria getCriteria() {
        return addOrder(getBaseCriteria()).addOrder(getNaturalOrder());
    }

    private Criteria getBaseCriteria() {
        Criteria criteria = hbnSessionManager.getHbnSession().createCriteria(type);
        if (filters != null) {
            for (ContainerFilter filter : filters) {
                String filterPropertyName = filter.propertyId.toString();

                if (propertyInEmbeddedKey(filterPropertyName)) {
                    String idName = getClassMetadata().getIdentifierPropertyName();
                    filterPropertyName = idName + "." + filterPropertyName;
                }

                boolean requireExactMatch = false;
                Object requiredMatch = null;
                try {
                    final Field property = type.getDeclaredField(filterPropertyName);
                    final Class<?> propertyType = property.getType();
                    if (propertyType.getSimpleName().equals("Boolean")) {
                        requireExactMatch = true;
                        requiredMatch = Boolean.valueOf(filter.filterString);
                    }
                } catch (NoSuchFieldException e) {
                    // can't happen really, and if it does hibernate will report
                    // error meaningfully
                }

                if (requireExactMatch) {
                    criteria = criteria.add(Restrictions.eq(filterPropertyName, requiredMatch));
                } else if (filter.ignoreCase) {
                    criteria = criteria.add(Restrictions.ilike(filterPropertyName, filter.filterString,
                            filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE));
                } else {
                    criteria = criteria.add(Restrictions.like(filterPropertyName, filter.filterString,
                            filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE));
                }
            }
        }
        // Allow subclasses to modify the criteria
        criteria = addSearchCriteria(criteria);

        return criteria;
    }

    private Order getNaturalOrder() {
        if (temporaryFlippedAsc) {
            return Order.asc(getIdPropertyName());
        } else {
            return Order.desc(getIdPropertyName());
        }
    }

    private Criteria addOrder(Criteria criteria) {
        if (orderPropertyIds != null) {
            for (int i = 0; i < orderPropertyIds.length; i++) {
                String orderPropertyId = orderPropertyIds[i].toString();
                if (propertyInEmbeddedKey(orderPropertyId)) {
                    String idName = getClassMetadata().getIdentifierPropertyName();
                    orderPropertyId = idName + "." + orderPropertyId;
                }
                boolean a = temporaryFlippedAsc ? orderAscendings[i] : !orderAscendings[i];
                if (a) {
                    criteria = criteria.addOrder(Order.asc(orderPropertyId));
                } else {
                    criteria = criteria.addOrder(Order.desc(orderPropertyId));
                }
            }
        }
        return criteria;
    }

    @Override
    public Object firstItemId() {
        if (firstId == null) {
            firstId = firstItemId(true);
        }
        return firstId;
    }

    public Object firstItemId(boolean byPassCache) {
        if (byPassCache) {
            Object first = getCriteria().setMaxResults(1).setCacheable(true).uniqueResult();
            return getIdForPojo(first);
        } else {
            return firstItemId();
        }
    }

    private Object getIdForPojo(Object pojo) {
        return getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return itemId.equals(firstItemId());
    }

    @Override
    public boolean isLastId(Object itemId) {
        return itemId.equals(lastItemId());
    }

    @Override
    public Object lastItemId() {
        if (lastId == null) {
            temporaryFlippedAsc = !temporaryFlippedAsc;
            lastId = firstItemId(true);
            temporaryFlippedAsc = !temporaryFlippedAsc;
        }
        return lastId;
    }

    /*
     * Simple method, but lot's of code :-)
     * 
     * Rather complicated logic is needed to avoid: - large number of db queries - scrolling through whole query result
     * 
     * This way this container can be used with large data sets.
     */
    @Override
    public Object nextItemId(Object itemId) {
        if (isLastId(itemId)) {
            return null;
        }

        EntityItem<T> item = new EntityItem<T>((Serializable) itemId);

        // check if next itemId is in current buffer
        List<?> buffer = getRowBuffer();
        try {
            int curBufIndex = buffer.indexOf(item.getPojo());
            if (curBufIndex != -1) {
                Object object = buffer.get(curBufIndex + 1);
                return getIdForPojo(object);
            }
        } catch (Exception e) {
            // not in buffer
        }

        // itemId was not in buffer
        // build query with current order and limiting result set with the
        // reference row. Then first result is next item.

        Criteria crit = getCriteria();
        Disjunction afterCurrent = Restrictions.disjunction();

        Criterion curEq = null;
        if (orderAscendings != null) {
            for (int i = 0; i < orderAscendings.length; i++) {
                String col = orderPropertyIds[i].toString();
                Object colVal = item.getItemProperty(col).getValue();

                boolean ruleAsc = temporaryFlippedAsc ? orderAscendings[i] : !orderAscendings[i];

                if (propertyInEmbeddedKey(col)) {
                    String idName = getClassMetadata().getIdentifierPropertyName();
                    col = idName + "." + col;
                }

                Criterion gtCurOrder;
                if (colVal == null) {
                    gtCurOrder = ruleAsc ? null : Restrictions.isNotNull(col);
                    curEq = Restrictions.isNull(col);
                } else {
                    gtCurOrder = ruleAsc ? Restrictions.gt(col, colVal) : Restrictions.lt(col, colVal);
                    curEq = Restrictions.eq(col, colVal);
                }
                if (gtCurOrder != null) {
                    afterCurrent.add(gtCurOrder);
                }

                if (i != 0) {
                    curEq = afterCurrent.add(Restrictions.and(curEq, Restrictions.eq(col, colVal)));
                }
            }
        }

        Criterion naturalGt;

        // For composite primary keys
        Type idType = getClassMetadata().getPropertyType(getIdPropertyName());
        final boolean idIsEmbeddedPk = idType.isComponentType();
        if (idIsEmbeddedPk) {
            Disjunction allPropertyGtCombos = Restrictions.disjunction();

            ComponentType idComponent = (ComponentType) idType;
            String idPropertyName = getClassMetadata().getIdentifierPropertyName();

            String[] idPropertyNames = idComponent.getPropertyNames();
            for (int i = 0; i < idPropertyNames.length; i++) {
                Conjunction propertyGtCombo = Restrictions.conjunction();
                String propertyName = idPropertyNames[i];
                Object propertyValue = idComponent.getPropertyValue(itemId, i, EntityMode.POJO);
                if (temporaryFlippedAsc) {
                    propertyGtCombo.add(Restrictions.gt(idPropertyName + "." + propertyName, propertyValue));
                } else {
                    propertyGtCombo.add(Restrictions.lt(idPropertyName + "." + propertyName, propertyValue));
                }

                // All the properties sorted outside of this one must be equal
                // to the
                // previous pojo's properties for this property to count
                for (int j = 0; j < i; j++) {
                    String superiorSortedProperty = idPropertyNames[j];
                    Object superiorSortedPropertyValue = idComponent.getPropertyValue(itemId, i, EntityMode.POJO);
                    propertyGtCombo.add(Restrictions.eq(idPropertyName + "." + superiorSortedProperty,
                            superiorSortedPropertyValue));
                }
                allPropertyGtCombos.add(propertyGtCombo);
            }
            naturalGt = allPropertyGtCombos;

        } else { // primary key is not embedded

            if (temporaryFlippedAsc) {
                naturalGt = Restrictions.gt(getIdPropertyName(), itemId);
            } else {
                naturalGt = Restrictions.lt(getIdPropertyName(), itemId);
            }
        }

        if (curEq != null) {
            afterCurrent.add(Restrictions.and(curEq, naturalGt));
            crit.add(afterCurrent);
        } else {
            crit.add(naturalGt);
        }

        crit = crit.setMaxResults(ROW_BUF_SIZE);
        List<?> newBuffer = crit.list();
        if (newBuffer.size() > 0) {
            // save buffer to optimize query count
            setRowBuffer(newBuffer);
            Object nextPojo = newBuffer.get(0);
            return getIdForPojo(nextPojo);
        } else {
            return null;
        }
    }

    /**
     * RowBuffer stores some pojos to avoid excessive number of DB queries.
     * 
     * @return
     */
    private List<?> getRowBuffer() {
        if (temporaryFlippedAsc) {
            return ascRowBuffer;
        } else {
            return descRowBuffer;
        }
    }

    /**
     * RowBuffer stores some pojos to avoid excessive number of DB queries.
     */
    private void setRowBuffer(List<?> list) {
        if (temporaryFlippedAsc) {
            ascRowBuffer = list;
        } else {
            descRowBuffer = list;
        }
    }

    /**
     * @return column name of identifier property
     */
    private String getIdPropertyName() {
        return getClassMetadata().getIdentifierPropertyName();
    }

    @Override
    public Object prevItemId(Object itemId) {
        // temp flip order and use nextItemId
        temporaryFlippedAsc = !temporaryFlippedAsc;
        Object prev = nextItemId(itemId);
        temporaryFlippedAsc = !temporaryFlippedAsc;
        return prev;
    }

    // Container.Indexed

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getIdByIndex(int index) {
        if (indexRowBuffer == null) {
            resetIndexRowBuffer(index);
        }
        int indexInCache = index - indexRowBufferFirstIndex;
        if (!(indexInCache >= 0 && indexInCache < indexRowBuffer.size())) {
            resetIndexRowBuffer(index);
            indexInCache = 0;
        }
        Object pojo = indexRowBuffer.get(indexInCache);
        Object id = getIdForPojo(pojo);
        idToIndex.put(id, new Integer(index));
        if (idToIndex.size() > ID_TO_INDEX_MAX_SIZE) {
            // clear one from beginning
            idToIndex.remove(idToIndex.keySet().iterator().next());
        }
        return id;
    }

    private void resetIndexRowBuffer(int index) {
        indexRowBufferFirstIndex = index;
        indexRowBuffer = getCriteria().setFirstResult(index).setMaxResults(ROW_BUF_SIZE).list();
    }

    /*
     * Note! Expects that getIdByIndex is called for this itemId. When used with Table, this shouldn't be a problem.
     * 
     * VAADIN_TODO make workaround for this. Too bad it is going to be a very slow operation.
     */
    @Override
    public int indexOfId(Object itemId) {
        Integer index = idToIndex.get(itemId);
        return index;
    }

    // Container.Sortable methods

    @Override
    public Collection<String> getSortableContainerPropertyIds() {
        // use Hibernates metadata helper to determine property names
        String[] propertyNames = getClassMetadata().getPropertyNames();
        LinkedList<String> propertyIds = new LinkedList<String>();
        propertyIds.addAll(Arrays.asList(propertyNames));
        propertyIds.addAll(getEmbeddedKeyPropertyIds());
        return propertyIds;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        clearInternalCache();
        orderPropertyIds = propertyId;
        orderAscendings = ascending;
    }

    private void clearInternalCache() {
        idToIndex.clear();
        indexRowBuffer = null;
        ascRowBuffer = null;
        descRowBuffer = null;
        firstId = null;
        lastId = null;
        size = null;
        // System.err.println("*** HbnContainer: clearInternalCache() done.");
    }

    class ContainerFilter {

        private final Object propertyId;
        private final String filterString;
        private final boolean onlyMatchPrefix;
        private final boolean ignoreCase;

        public ContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) {
            this.propertyId = propertyId;
            this.ignoreCase = ignoreCase;
            this.filterString = ignoreCase ? filterString.toLowerCase() : filterString;
            this.onlyMatchPrefix = onlyMatchPrefix;
        }

    }

    /**
     * Adds container filter for hibernate mapped property. For property not mapped by Hibernate, {@link UnsupportedOperationException} is
     * thrown.
     * 
     * @see Container.Filterable#addContainerFilter(Object, String, boolean, boolean)
     */
    @Override
    public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) {
        if (addedProperties.containsKey(propertyId)) {
            throw new UnsupportedOperationException(
                    "HbnContainer does not support filtering properties not mapped by Hibernate");
        } else {
            if (filters == null) {
                filters = new HashSet<ContainerFilter>();
            }
            ContainerFilter f = new ContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix);
            filters.add(f);
            clearInternalCache();
            fireItemSetChange();
        }
    }

    @Override
    public void removeAllContainerFilters() {
        if (filters != null) {
            filters = null;
            clearInternalCache();
            fireItemSetChange();
        }
    }

    @Override
    public void removeContainerFilters(Object propertyId) {
        if (filters != null) {
            for (Iterator<ContainerFilter> iterator = filters.iterator(); iterator.hasNext();) {
                ContainerFilter f = iterator.next();
                if (f.propertyId.equals(propertyId)) {
                    iterator.remove();
                }
            }
            clearInternalCache();
            fireItemSetChange();
        }
    }

    public Set<ContainerFilter> getContainerFilters() {
        return filters;
    }

    /**
     * We want to flush the container cache at the end of each transaction. In this way, we don't get old objects displayed when repainting
     * occurs.
     */
    private void attachVaadinTransactionListener() {
        if (hbnSessionManager == null)
            return;
        if (!(hbnSessionManager instanceof Application))
            return;

        final ApplicationContext context = ((Application) hbnSessionManager).getContext();
        if (context == null)
            return;
        context.addTransactionListener(new TransactionListener() {
            private static final long serialVersionUID = -7727149551252570994L;

            @Override
            public void transactionEnd(Application application, Object transactionData) {
                // System.err.println("HbnContainer: transactionEnd class="+application.getClass());
                // Transaction listener gets fired for all (Http) sessions
                // of Vaadin applications, checking to be this one.
                if (application == ((Application) hbnSessionManager)) {
                    clearInternalCache();
                    hbnSessionManager.getHbnSession().close();
                    // System.err.println("cache cleared for application "+hbnSessionManager.hashCode());
                } else {
                    // System.err.println("application:"+application.hashCode()+"not me:"+hbnSessionManager.hashCode());
                }
            }

            @Override
            public void transactionStart(Application application, Object transactionData) {
            }
        });
    }

    public void clearCache() {
        this.clearInternalCache();
    }

    /**
     * This class adds filtering criteria other than simple textual filtering as implemented by the Filterable interface.
     * 
     * @param criteria
     *            The base criteria as this class implements it with the Filterable search.
     * @return The new search criteria, possibly modified with extra restrictions or replaced completely.
     */
    public Criteria addSearchCriteria(Criteria criteria) {
        // No modification necessary in this class. It is only for the
        // subclasses to use.
        return criteria;
    }

    /**
     * <p>
     * Perform introspection on a Java Bean class to find its properties.
     * </p>
     * 
     * <p>
     * Note : This version only supports introspectable bean properties and their getter and setter methods. Stand-alone <code>is</code> and
     * <code>are</code> methods are not supported.
     * </p>
     * 
     * @param beanClass
     *            the Java Bean class to get properties for.
     * @return an ordered map from property names to property descriptors
     */
    static LinkedHashMap<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> beanClass) {
        final LinkedHashMap<String, PropertyDescriptor> pdMap = new LinkedHashMap<String, PropertyDescriptor>();

        // Try to introspect, if it fails, we just have an empty Item
        try {
            final BeanInfo info = Introspector.getBeanInfo(beanClass);
            final PropertyDescriptor[] pds = info.getPropertyDescriptors();

            // Add all the bean properties as MethodProperties to this Item
            for (int i = 0; i < pds.length; i++) {
                final Method getMethod = pds[i].getReadMethod();
                if ((getMethod != null) && getMethod.getDeclaringClass() != Object.class) {
                    pdMap.put(pds[i].getName(), pds[i]);
                }
            }
        } catch (final java.beans.IntrospectionException ignored) {
        }

        return pdMap;
    }

}
