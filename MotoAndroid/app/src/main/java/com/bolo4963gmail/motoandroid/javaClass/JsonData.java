package com.bolo4963gmail.motoandroid.javaClass;

import java.util.List;

public class JsonData {

    /**
     * _class : hudson.model.FreeStyleBuild
     * actions : [{"_class":"hudson.model.CauseAction","causes":[{"_class":"hudson.model.Cause$UserIdCause","shortDescription":"Started by user Mu Fengjun","userId":"mufengjun260","userName":"Mu Fengjun"}]},{"_class":"hudson.plugins.git.util.BuildData","buildsByBranchName":{"refs/remotes/origin/master":{"_class":"hudson.plugins.git.util.Build","buildNumber":45,"buildResult":null,"marked":{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","branch":[{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","name":"refs/remotes/origin/master"}]},"revision":{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","branch":[{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","name":"refs/remotes/origin/master"}]}}},"lastBuiltRevision":{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","branch":[{"SHA1":"62d7bfb7355920748ea8c7d35fc4d5562adeaaf2","name":"refs/remotes/origin/master"}]},"remoteUrls":["https://github.com/mufengjun260/motoAndroid.git"],"scmName":""},{"_class":"hudson.plugins.git.GitTagAction"},{},{},{}]
     * artifacts : []
     * building : false
     * description : null
     * displayName : #45
     * duration : 3569
     * estimatedDuration : 2600
     * executor : null
     * fullDisplayName : testOne #45
     * id : 45
     * keepLog : false
     * number : 45
     * queueId : 53
     * result : FAILURE
     * timestamp : 1468489777001
     * url : http://115.29.114.77/job/testOne/45/
     * builtOn :
     * changeSet : {"_class":"hudson.plugins.git.GitChangeSetList","items":[],"kind":"git"}
     * culprits : []
     */

    private String _class;
    private boolean building;
    private Object description;
    private String displayName;
    private int duration;
    private int estimatedDuration;
    private Object executor;
    private String fullDisplayName;
    private String id;
    private boolean keepLog;
    private int number;
    private int queueId;
    private String result;
    private long timestamp;
    private String url;
    private String builtOn;
    /**
     * _class : hudson.plugins.git.GitChangeSetList
     * items : []
     * kind : git
     */

    private ChangeSetBean changeSet;
    /**
     * _class : hudson.model.CauseAction
     * causes : [{"_class":"hudson.model.Cause$UserIdCause","shortDescription":"Started by user Mu Fengjun","userId":"mufengjun260","userName":"Mu Fengjun"}]
     */

    private List<ActionsBean> actions;
    private List<?> artifacts;
    private List<?> culprits;

    public String get_class() { return _class;}

    public void set_class(String _class) { this._class = _class;}

    public boolean isBuilding() { return building;}

    public void setBuilding(boolean building) { this.building = building;}

    public Object getDescription() { return description;}

    public void setDescription(Object description) { this.description = description;}

    public String getDisplayName() { return displayName;}

    public void setDisplayName(String displayName) { this.displayName = displayName;}

    public int getDuration() { return duration;}

    public void setDuration(int duration) { this.duration = duration;}

    public int getEstimatedDuration() { return estimatedDuration;}

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Object getExecutor() { return executor;}

    public void setExecutor(Object executor) { this.executor = executor;}

    public String getFullDisplayName() { return fullDisplayName;}

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getId() { return id;}

    public void setId(String id) { this.id = id;}

    public boolean isKeepLog() { return keepLog;}

    public void setKeepLog(boolean keepLog) { this.keepLog = keepLog;}

    public int getNumber() { return number;}

    public void setNumber(int number) { this.number = number;}

    public int getQueueId() { return queueId;}

    public void setQueueId(int queueId) { this.queueId = queueId;}

    public String getResult() { return result;}

    public void setResult(String result) { this.result = result;}

    public long getTimestamp() { return timestamp;}

    public void setTimestamp(long timestamp) { this.timestamp = timestamp;}

    public String getUrl() { return url;}

    public void setUrl(String url) { this.url = url;}

    public String getBuiltOn() { return builtOn;}

    public void setBuiltOn(String builtOn) { this.builtOn = builtOn;}

    public ChangeSetBean getChangeSet() { return changeSet;}

    public void setChangeSet(ChangeSetBean changeSet) { this.changeSet = changeSet;}

    public List<ActionsBean> getActions() { return actions;}

    public void setActions(List<ActionsBean> actions) { this.actions = actions;}

    public List<?> getArtifacts() { return artifacts;}

    public void setArtifacts(List<?> artifacts) { this.artifacts = artifacts;}

    public List<?> getCulprits() { return culprits;}

    public void setCulprits(List<?> culprits) { this.culprits = culprits;}

    public static class ChangeSetBean {

        private String _class;
        private String kind;
        private List<?> items;

        public String get_class() { return _class;}

        public void set_class(String _class) { this._class = _class;}

        public String getKind() { return kind;}

        public void setKind(String kind) { this.kind = kind;}

        public List<?> getItems() { return items;}

        public void setItems(List<?> items) { this.items = items;}
    }

    public static class ActionsBean {

        private String _class;
        /**
         * _class : hudson.model.Cause$UserIdCause
         * shortDescription : Started by user Mu Fengjun
         * userId : mufengjun260
         * userName : Mu Fengjun
         */

        private List<CausesBean> causes;

        public String get_class() { return _class;}

        public void set_class(String _class) { this._class = _class;}

        public List<CausesBean> getCauses() { return causes;}

        public void setCauses(List<CausesBean> causes) { this.causes = causes;}

        public static class CausesBean {

            private String _class;
            private String shortDescription;
            private String userId;
            private String userName;

            public String get_class() { return _class;}

            public void set_class(String _class) { this._class = _class;}

            public String getShortDescription() { return shortDescription;}

            public void setShortDescription(String shortDescription) {
                this.shortDescription = shortDescription;
            }

            public String getUserId() { return userId;}

            public void setUserId(String userId) { this.userId = userId;}

            public String getUserName() { return userName;}

            public void setUserName(String userName) { this.userName = userName;}
        }
    }
}
