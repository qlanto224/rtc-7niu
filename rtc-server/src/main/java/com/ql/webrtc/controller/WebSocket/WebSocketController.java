package com.ql.webrtc.controller.WebSocket;

import com.alibaba.fastjson.JSONObject;
import com.ql.webrtc.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket业务类
 * 此类用来作为RTC的信令服务器
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{uname}") //此注解相当于设置访问URL
public class WebSocketController {

    private Session session;
    private String uname;
    private static CopyOnWriteArraySet<WebSocketController> webSockets = new CopyOnWriteArraySet<>();
    private static Map<String, Session> sessionPool = new HashMap<String, Session>();


    @OnOpen
    public void onOpen(Session session, @PathParam(value = "uname") String uname) {
        try {
            this.session = session;
            this.uname = uname;
            //删除旧的 保证在线账号只存在一个
            webSockets.removeIf(w -> uname.equals(w.uname));
            webSockets.add(this);
            sessionPool.put(uname, session);
            JSONObject obj = new JSONObject();
            obj.put("data", this.onlineList());
            obj.put("type", "userOnlineList");
            sendAllMessage(obj.toJSONString());
            log.info("【websocket消息】有新的连接，总数为:" + webSockets.size());
        } catch (Exception e) {
        }
    }

    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            sessionPool.remove(this.uname);
            JSONObject obj = new JSONObject();
            obj.put("data", this.onlineList());
            obj.put("type", "userOnlineList");
            sendAllMessage(obj.toJSONString());
            log.info("【websocket消息】连接断开，总数为:" + webSockets.size());
        } catch (Exception e) {
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("【websocket消息】收到客户端消息:" + message);
        JSONObject msg = JSONObject.parseObject(message);
        String cmd = msg.getString("cmd");
        JSONObject from = new JSONObject();
        switch (cmd) {
            case Constant.TOPIC:
                sendAllMessage(msg.toJSONString());
                break;
            case Constant.USERS:
                from.put("uname", this.uname);
                msg.put("from", from);
                String toUsers = msg.getString("toUsers");
                sendMoreMessage(toUsers.split(","), msg.toJSONString());
                break;
            case Constant.USER:
                from.put("uname", this.uname);
                msg.put("from", from);
                String user = msg.getString("toUser");
                sendOneMessage(user, msg.toJSONString());
                break;
            default:
                break;
        }
    }

    // 此为广播消息
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:" + message);
        for (WebSocketController webSocket : webSockets) {
            try {
                if (webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 此为单点消息
    public void sendOneMessage(String userId, String message) {
        Session sess = sessionPool.get(userId);
        try {
            if (sess != null && sess.isOpen()) {
                sess.getAsyncRemote().sendText(message);
                log.info("【websocket消息】 单点消息:" + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 此为单点消息(多人)
    public void sendMoreMessage(String[] userIds, String message) {

        for (String userId : userIds) {
            Session session = sessionPool.get(userId);
            try {
                if (session != null && session.isOpen()) {
                    log.info("【websocket消息】 单点消息:" + message);
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Map<String, String>> onlineList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (WebSocketController webSocket : this.webSockets) {
            Map<String, String> map = new HashMap<>();
            map.put("uname", webSocket.uname);
            list.add(map);
        }
        return list;
    }
}
