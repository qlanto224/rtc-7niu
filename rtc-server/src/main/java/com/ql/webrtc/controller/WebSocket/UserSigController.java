package com.ql.webrtc.controller.WebSocket;

import com.qiniu.rtc.RtcRoomManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author qinlei
 * @description todo
 * @date 2021/6/8 17:34
 */
@RestController
@RequestMapping
@Slf4j
public class UserSigController {

    @Value("${qiniu.accessKey}")
    private String accessKey;
    @Value("${qiniu.secretKey}")
    private String secretKey;

    private String auth(String appid, long expireAt, String roomname, String userid, String permission){
        Auth auth = Auth.create(accessKey, secretKey);
        RtcRoomManager rmanager = new RtcRoomManager(auth);
        try {
            String token = rmanager.getRoomToken(appid, roomname, userid, expireAt, permission);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("身份认证文件生成失败!",e);
            return null;
        }
    }

    @GetMapping("/createToken")
    public String createToken(@RequestParam("ad") String appid, @RequestParam("roomname") String roomname,
                              @RequestParam("userid") String userid,@RequestParam("permission") String permission,
                              @RequestParam("expireAt") long expireAt){
        return this.auth(appid,expireAt,roomname,userid,permission);
    }
}
