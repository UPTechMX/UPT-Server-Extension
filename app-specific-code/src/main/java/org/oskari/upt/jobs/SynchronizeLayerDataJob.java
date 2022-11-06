package org.oskari.upt.jobs;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.util.Date;
import java.util.Map;
import org.oskari.upt.db.SynchronizeDatabase;

@Oskari("SynchronizeLayerDataJob")
public class SynchronizeLayerDataJob extends fi.nls.oskari.worker.ScheduledJob {
  private static final Logger LOG = LogFactory.getLogger(
      SynchronizeLayerDataJob.class);

  @Override
  public void execute(Map<String, Object> params) {
    LOG.info("Synchronizing CKAN layers. The time is " + new Date());
    SynchronizeDatabase syncDb = new SynchronizeDatabase();
    syncDb.synchronizeLayersFromCKAN();
  }
}
