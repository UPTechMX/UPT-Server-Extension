package flyway.upt;

import org.oskari.helpers.AppSetupHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.util.List;

/**
 * Adds download-basket bundle to default and user views.
 */
public class V1_0_0__add_download_basket extends BaseJavaMigration {
    private static final String BUNDLE_ID = "download-basket";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        final List<Long> views = AppSetupHelper.getSetupsForUserAndDefaultType(connection);
        for (Long viewId : views) {
            if (AppSetupHelper.appContainsBundle(connection, viewId, BUNDLE_ID)) {
                continue;
            }
            AppSetupHelper.addBundleToApp(connection, viewId, BUNDLE_ID);
        }
    }
}