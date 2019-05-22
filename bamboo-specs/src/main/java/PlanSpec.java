import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.notification.Notification;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.notification.PlanFailedNotification;
import com.atlassian.bamboo.specs.builders.notification.ResponsibleRecipient;
import com.atlassian.bamboo.specs.builders.notification.WatchersRecipient;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

@BambooSpec
public class PlanSpec {
    public Plan plan() {
        final Plan plan = new Plan(new Project()
                .oid(new BambooOid("2ej6lgrjg8w2"))
                .key(new BambooKey("MAIL"))
                .name("Mailcheck"),
            "mailcheck-landing.firebaseapp.com",
            new BambooKey("MLAND"))
            .oid(new BambooOid("2e9hdvebml8n"))
            .pluginConfigurations(new ConcurrentBuilds()
                    .useSystemWideDefault(false)
                    .maximumNumberOfConcurrentBuilds(20))
            .stages(new Stage("Stage 1")
                    .jobs(new Job("Job 1",
                            new BambooKey("JOB1"))
                            .tasks(new VcsCheckoutTask()
                                    .checkoutItems(new CheckoutItem().defaultRepository()),
                                new ScriptTask()
                                    .fileFromPath("bamboo-specs/build.sh"))))
            .linkedRepositories("Mailcheck / mailcheck.landing - master")
            .triggers(new BitbucketServerTrigger())
            .planBranchManagement(new PlanBranchManagement()
                    .createForVcsBranch()
                    .delete(new BranchCleanup()
                        .whenRemovedFromRepositoryAfterDays(1)
                        .whenInactiveInRepositoryAfterDays(30))
                    .notificationLikeParentPlan())
            .notifications(new Notification()
                    .type(new PlanFailedNotification())
                    .recipients(new ResponsibleRecipient(),
                        new WatchersRecipient()))
            .forceStopHungBuilds();
        return plan;
    }

    public PlanPermissions planPermission() {
        final PlanPermissions planPermission = new PlanPermissions(new PlanIdentifier("MAIL", "MLAND"))
            .permissions(new Permissions()
                    .groupPermissions("Project_MAIL_AUTH_LVL_I",   PermissionType.VIEW, PermissionType.BUILD, PermissionType.EDIT, PermissionType.CLONE, PermissionType.ADMIN)
                    .groupPermissions("Project_MAIL_AUTH_LVL_II",  PermissionType.VIEW, PermissionType.BUILD, PermissionType.EDIT, PermissionType.CLONE)
                    .groupPermissions("Project_MAIL_AUTH_LVL_III", PermissionType.VIEW, PermissionType.BUILD, PermissionType.EDIT)
                    .groupPermissions("Project_MAIL_AUTH_LVL_IV",  PermissionType.VIEW, PermissionType.BUILD)
                    .groupPermissions("Project_MAIL_AUTH_LVL_V",   PermissionType.VIEW)
            );
        return planPermission;
    }

    public static void main(String... argv) {
        BambooServer bambooServer = new BambooServer("https://ci.nodeart.app");
        final PlanSpec planSpec = new PlanSpec();
        final Plan plan = planSpec.plan();
        bambooServer.publish(plan);
        final PlanPermissions planPermission = planSpec.planPermission();
        bambooServer.publish(planPermission);
    }
}
