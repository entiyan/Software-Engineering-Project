package jdm.model;

public class LabResultGroup {
    private final String groupId;
    private final String groupName;

    public LabResultGroup(String groupId, String groupName) {
        this.groupId   = groupId;
        this.groupName = groupName;
    }

    public String getGroupId()   { return groupId; }
    public String getGroupName() { return groupName; }

    @Override public String toString() { return groupName; }
}
