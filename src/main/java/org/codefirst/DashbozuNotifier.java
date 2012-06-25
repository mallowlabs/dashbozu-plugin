package org.codefirst;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

public class DashbozuNotifier extends Notifier {
    private String baseUrl;

    private static final Logger LOG = Logger.getLogger(DashbozuNotifier.class);

    @DataBoundConstructor
    public DashbozuNotifier(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        String jenkinsUrl = Hudson.getInstance().getRootUrl();

        if (StringUtils.isBlank(jenkinsUrl)) {
            return false;
        }

        String projectName = build.getProject().getName();

        String apiUrl = baseUrl + "/hook/jenkins?url=" + jenkinsUrl + "&project=" + projectName;
        System.out.println(apiUrl);

        URL url = new URL(apiUrl);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.connect();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(http.getInputStream());
            while (bis.read() != -1) {
                // do nothing
            }
        } catch (IOException e) {
            // ignore because notification failures make build failures!
            LOG.error("[Dashbozu] Failed to notification", e);
        } finally {
            IOUtils.closeQuietly(bis);
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/dashbozu-plugin/DashbozuNotifier.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> project) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Dashbozu";
        }
    }
}
