package cz.uhk.stex.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cz.uhk.stex.Database;
import cz.uhk.stex.LoggedInUser;
import cz.uhk.stex.MainActivity;
import cz.uhk.stex.R;
import cz.uhk.stex.model.FeedItem;
import cz.uhk.stex.model.Group;
import cz.uhk.stex.util.ObservableList;
import cz.uhk.stex.util.Promise;

/**
 * Notification service
 * Created by Karelp on 12.07.2016.
 */
public class NotificationService extends Service {

    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final Map<String, Integer> feedItemToNotificationMap = new HashMap<>();
    private final Database.FeedItemReadListener feedItemReadListener = new Database.FeedItemReadListener() {
        @Override
        public void itemRead(FeedItem feedItem, String readByUserId) {
            removeNotification(feedItem.getId());
        }
    };

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Void>() {
            @Override
            public Void onSuccess(final LoggedInUser loggedInUser) throws Exception {
                for (Group group : loggedInUser.getGroups()) {
                    ObservableList<FeedItem> feed = Database.getGroupFeed(group.getId(), 4); // Limits the number of displayed notifications when the app starts
                    feed.addItemAddListener(new ObservableList.ItemAddListener<FeedItem>() {
                        @Override
                        public void onItemAdded(@NotNull ObservableList<FeedItem> list, @NotNull Collection<FeedItem> addedItems) {
                            for (FeedItem addedItem : addedItems) {
                                if (!addedItem.getReadBy().containsKey(loggedInUser.getUser().getId()))
                                    notifyNewFeedItem(addedItem, loggedInUser.getUser().getId());
                            }
                        }
                    });
                }
                return null;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    private Notification notifyNewFeedItem(FeedItem feedItem, String userId) {
        Notification res = new Notification.Builder(getApplicationContext())
                .setContentTitle(feedItem.getTitle())
                .setContentText(feedItem.getText())
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(getBaseContext(), 0, new Intent(getBaseContext(), MainActivity.class), 0))
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int id = feedItemToNotificationMap.containsKey(feedItem.getId()) ? feedItemToNotificationMap.get(feedItem.getId()) : idGenerator.incrementAndGet();
        feedItemToNotificationMap.put(feedItem.getId(), id);
        manager.notify(feedItem.getId(), id, res);
        Database.observeFeedItemRead(feedItem, userId, feedItemReadListener);
        return res;
    }

    private void removeNotification(@Nullable String id) {
        if (id == null)
            return;

        Integer notId = feedItemToNotificationMap.get(id);
        if (notId != null) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(id, notId);
            feedItemToNotificationMap.remove(id);
        }
    }
}
