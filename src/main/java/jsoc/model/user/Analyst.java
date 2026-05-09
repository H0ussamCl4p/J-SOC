package jsoc.model.user;

import java.util.Set;

public class Analyst extends User {

    private static final Set<String> ALLOWED_ACTIONS = Set.of(
            ACTION_CREATE_INCIDENT,
            ACTION_VIEW_ASSIGNED,
            ACTION_COMMENT,
            ACTION_UPDATE_STATUS
    );

    public Analyst(String username, String password, String email) {
        super(username, password, email);
    }

    public Analyst(long id, String username, String password, String email) {
        super(id, username, password, email);
    }

    @Override
    public String getRole() {
        return "ANALYST";
    }

    @Override
    public boolean canPerformAction(String action) {
        return ALLOWED_ACTIONS.contains(action);
    }
}
