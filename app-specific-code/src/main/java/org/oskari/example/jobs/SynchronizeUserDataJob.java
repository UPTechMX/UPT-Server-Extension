package org.oskari.example.jobs;

import org.oskari.example.db.SynchronizeDatabase;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.Date;
import java.util.Map;

@Oskari("SynchronizeUserDataJob")
public class SynchronizeUserDataJob extends fi.nls.oskari.worker.ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(SynchronizeUserDataJob.class);

    @Override
    public void execute(Map<String, Object> params) {
        LOG.info("Synchronizing CKAN users and user groups. The time is " + new Date());
        SynchronizeDatabase syncDb = new SynchronizeDatabase();
        syncDb.synchronizeUsersWithRolesFromCKAN();
    }
}