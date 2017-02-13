package com.hypersocket.bulk.json;

import com.hypersocket.events.EventService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.Session;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class BulkAssignmentServiceImpl implements BulkAssignmentService {

    public static final String RESOURCE_BUNDLE = "BulkAssignmentService";

    @Autowired
    EventService eventService;

    @Autowired
    RealmService realmService;

    @Autowired
    BulkAssignmentRepository bulkAssignmentRepository;

    @Autowired
    TransactionService transactionService;

    @PostConstruct
    private void postConstruct() {
        eventService.registerEvent(BulkAssignmentEvent.class, RESOURCE_BUNDLE);
    }

    @Override
    public void bulkAssignRolesToResource(final BulkAssignment bulkAssignment) throws ResourceException {

        final List<Long> roleIds = bulkAssignment.getRoleIds();
        final List<Long> resourceIds = bulkAssignment.getResourceIds();
        final BulkAssignmentMode bulkAssignmentMode = bulkAssignment.getMode();

        if(roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be empty.");
        }

        if(resourceIds == null || resourceIds.isEmpty()) {
            throw new IllegalArgumentException("Resources cannot be empty.");
        }


        transactionService.doInTransaction(new TransactionCallbackWithError<Void>() {

            Principal principal = realmService.getCurrentPrincipal();
            Realm realm = realmService.getCurrentRealm();
            final Session session = realmService.getCurrentSession();
            final RealmProvider realmProvider = realmService.getProviderForRealm(realm);

            @Override
            public Void doInTransaction(TransactionStatus transactionStatus) {

                bulkAssignmentRepository.bulkAssignRolesToResource(roleIds, resourceIds, bulkAssignmentMode);
                eventService.publishEvent(new BulkAssignmentEvent(this, roleIds, resourceIds, bulkAssignmentMode,
                        session, realm,
                        realmProvider, principal));
                return null;
            }

            @Override
            public void doTransacationError(Throwable e) {
                eventService.publishEvent(new BulkAssignmentEvent(this, roleIds, resourceIds, bulkAssignmentMode,
                        e, session, realm,
                        realmProvider, principal));
            }
        });

    }
}
