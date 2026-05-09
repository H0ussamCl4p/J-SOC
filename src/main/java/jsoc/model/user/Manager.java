package jsoc.model.user;

import java.util.Set;

public class Manager extends User {

    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            ACTION_CREATE_INCIDENT,
            ACTION_VIEW_ASSIGNED,
            ACTION_COMMENT,
            ACTION_UPDATE_STATUS,
            ACTION_ASSIGN,
            ACTION_CLOSE,
            ACTION_VIEW_STATS,
            ACTION_DELETE
    );

    public Manager(String username, String password, String email) {
        super(username, password, email);
    }

    public Manager(long id, String username, String password, String email) {
        super(id, username, password, email);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    @Override
    public boolean canPerformAction(String action) {
        return ALLOWED_ACTIONS.contains(action);
    }
}
