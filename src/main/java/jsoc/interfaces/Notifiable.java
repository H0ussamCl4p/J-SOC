package jsoc.interfaces;

import java.util.List;

public interface Notifiable {

    void notify(String message);

    List<String> getNotifications();

    void clearNotifications();
}
