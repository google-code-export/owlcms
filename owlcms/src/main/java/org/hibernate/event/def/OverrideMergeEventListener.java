/* 
 * Copyright ©2009 Jean-François Lamy
 * 
 * Licensed under the Open Software Licence, Version 3.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.opensource.org/licenses/osl-3.0.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.hibernate.event.def;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.WrongClassException;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.intercept.FieldInterceptionHelper;
import org.hibernate.intercept.FieldInterceptor;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author jflamy
 * 
 */
@SuppressWarnings("serial")
public class OverrideMergeEventListener extends DefaultMergeEventListener {
    private static final Log log = LogFactory.getLog(DefaultMergeEventListener.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void entityIsDetached(MergeEvent event, Map copyCache) {

        log.trace("merging detached instance");

        final Object entity = event.getEntity();
        final EventSource source = event.getSession();

        final EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
        final String entityName = persister.getEntityName();

        Serializable id = event.getRequestedId();
        if (id == null) {
            id = persister.getIdentifier(entity, source.getEntityMode());
        } else {
            // check that entity id = requestedId
            Serializable entityId = persister.getIdentifier(entity, source.getEntityMode());
            if (!persister.getIdentifierType().isEqual(id, entityId, source.getEntityMode(), source.getFactory())) {
                throw new HibernateException("merge requested with id not matching id of passed entity");
            }
        }

        String previousFetchProfile = source.getFetchProfile();
        source.setFetchProfile("merge");
        // we must clone embedded composite identifiers, or
        // we will get back the same instance that we pass in
        final Serializable clonedIdentifier = (Serializable) persister.getIdentifierType().deepCopy(id,
            source.getEntityMode(), source.getFactory());
        final Object result = source.get(entityName, clonedIdentifier);
        source.setFetchProfile(previousFetchProfile);

        if (result == null) {
            // this is in an attempt to fix the fact that if someone deletes a
            // lifter
            // who is currently lifting, there will be clones in the database.

            // TODO: we should throw an exception if we really *know* for sure
            // that this is a detached instance, rather than just assuming
            throw new StaleObjectStateException(entityName, id);

            // we got here because we assumed that an instance
            // with an assigned id was detached, when it was
            // really persistent
            // entityIsTransient(event, copyCache);
        } else {
            copyCache.put(entity, result); // before cascade!

            final Object target = source.getPersistenceContext().unproxy(result);
            if (target == entity) {
                throw new AssertionFailure("entity was not detached");
            } else if (!source.getEntityName(target).equals(entityName)) {
                throw new WrongClassException("class of the given object did not match class of persistent copy", event
                        .getRequestedId(), entityName);
            } else if (isVersionChanged(entity, source, persister, target)) {
                if (source.getFactory().getStatistics().isStatisticsEnabled()) {
                    source.getFactory().getStatisticsImplementor().optimisticFailure(entityName);
                }
                throw new StaleObjectStateException(entityName, id);
            }

            // cascade first, so that all unsaved objects get their
            // copy created before we actually copy
            cascadeOnMerge(source, persister, entity, copyCache);
            copyValues(persister, entity, target, source, copyCache);

            // copyValues works by reflection, so explicitly mark the entity
            // instance dirty
            markInterceptorDirty(entity, target);

            event.setResult(result);
        }

    }

    private void markInterceptorDirty(final Object entity, final Object target) {
        if (FieldInterceptionHelper.isInstrumented(entity)) {
            FieldInterceptor interceptor = FieldInterceptionHelper.extractFieldInterceptor(target);
            if (interceptor != null) {
                interceptor.dirty();
            }
        }
    }

    private boolean isVersionChanged(Object entity, EventSource source, EntityPersister persister, Object target) {
        if (!persister.isVersioned()) {
            return false;
        }
        // for merging of versioned entities, we consider the version having
        // been changed only when:
        // 1) the two version values are different;
        // *AND*
        // 2) The target actually represents database state!
        //
        // This second condition is a special case which allows
        // an entity to be merged during the same transaction
        // (though during a seperate operation) in which it was
        // originally persisted/saved
        boolean changed = !persister.getVersionType().isSame(persister.getVersion(target, source.getEntityMode()),
            persister.getVersion(entity, source.getEntityMode()), source.getEntityMode());

        // TODO : perhaps we should additionally require that the incoming
        // entity
        // version be equivalent to the defined unsaved-value?
        return changed && existsInDatabase(target, source, persister);
    }

    private boolean existsInDatabase(Object entity, EventSource source, EntityPersister persister) {
        EntityEntry entry = source.getPersistenceContext().getEntry(entity);
        if (entry == null) {
            Serializable id = persister.getIdentifier(entity, source.getEntityMode());
            if (id != null) {
                EntityKey key = new EntityKey(id, persister, source.getEntityMode());
                Object managedEntity = source.getPersistenceContext().getEntity(key);
                entry = source.getPersistenceContext().getEntry(managedEntity);
            }
        }

        if (entry == null) {
            // perhaps this should be an exception since it is only ever used
            // in the above method?
            return false;
        } else {
            return entry.isExistsInDatabase();
        }
    }
}
