<template>
  <div>
    <div class="box">
      <a-list item-layout="horizontal" :data-source="userList" v-if="!aCallShow">
        <a-list-item slot="renderItem" slot-scope="item, index">
          <a-list-item-meta :description="item.uname">
            <div slot="title" class="title">{{ item.uname }}<span v-if="item.uname == uname">（我）</span>
            </div>
            <a-avatar :size="48" slot="avatar" :style="`backgroundColor: ${item.uname == uname?'#40E0D0':'#87d068'}`"
              icon="user" />
          </a-list-item-meta>
          <div v-if="item.uname != uname">
            <img src="@/assets/mediaIcon/call.png" alt="通话" class="a_call" @click="tryCall(item.uname)" />
          </div>
        </a-list-item>
      </a-list>
      <div>
        <div v-if="aCallShow">
          <div id="remote" style="height: 100vh;background: linear-gradient(#aaaeb4, #636b76)">
          </div>
          <div id="local" class="splitter">
          </div>
        </div>

        <div class="aCallBtnBox">
          <a-button shape="round" type="danger" size="large" class="aCallBtn" v-show="isLinked" @click="hangup">
            <img src="@/assets/mediaIcon/end.png" alt="" />
            结束通话
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import Vue from 'vue'
  import * as QNRTC from "pili-rtc-web";
  import {
    Dialog
  } from "vant";
  Vue.use(Dialog);
  export default {
    name: "room",
    data() {
      return {
        localTrackList: [],
        publishTrackList: [],
        remoteTrackList: [],
        websock: null,
        target: "",
        userList: [],
        isLinked: false,
        aCallShow: false,
        appid: '******',//自己的appid
        roomtoken: "",
        uname: "",
        myRoom: null,
        retryNum: 2,
        out: null,
      };
    },
    created() {
    },
    mounted() {
      this.uname = Vue.ls.get("uname");
      console.log(this.uname)
      if (!this.uname) {
        window.location.replace("/");
      }
      this.initWebSocket()
      this.myRoom = new QNRTC.TrackModeSession();
      console.log("current version``````````````", QNRTC.version);
    },
    beforeDestory() {
      if (this.websock) {
        this.websock.close()
      }
    },
    methods: {
      quit() {
        history.back()
      },
      joinRoom() {
        return new Promise((resolve, reject) => {
                    var date1 = new Date();
                    var date2 = new Date(date1);
                    date2.setDate(date1.getDate() + 1);
                    this.$axios.get(window._CONFIG['qMediaSockURL'] + "/createToken", {
                        params: {
                            ad: this.appid,
                            roomname: this.roomname,
                            userid: this.uname,
                            permission: "user",
                            expireAt: date2.getTime()
                        }
                    }).then((res) => {
                        if (res.status == 200) {
                            let roomtoken = res.data
                            this.roomtoken = roomtoken
                            // 这里替换成刚刚生成的 RoomToken
                            this.myRoom.joinRoomWithToken(this.roomtoken).then(() => {
                                console.log("joinRoom success!");
                                this.aCallShow = true
                                this.isLinked = true
                                resolve()
                            }).catch((err) => {
                                reject(err)
                            })
                        }
                    }).catch((err) => {
                        reject(err)
                    })
                })
      },
      publish() {
        QNRTC.deviceManager.getLocalTracks({
          audio: {
            enabled: true,
            tag: "audio"
          },
          video: {
            enabled: true,
            tag: "video",
            facingMode: "environment"
          },
        }).then((localTracks) => {
          if (localTracks.length === 0) {
            return;
          }
          console.log("本地音视频流", localTracks);
          for (let localTrack of localTracks) {
            if (localTrack.info.trackId) {
              this.localTrackList.push(localTrack)
            }
            if (localTrack.info.tag === 'video') {
              localTrack.setMaster(true);
            }
            if (localTrack.info.tag === 'audio') {
              localTrack.setMaster(true);
            }
          }
          // 将刚刚的 Track 列表发布到房间中
          this.myRoom.publish(localTracks).then(() => {
            // 获取页面上的一个元素作为播放画面的父元素
            const localElement = document.getElementById("local");
            // 遍历本地采集的 Track 对象
            for (const localTrack of localTracks) {
              if (localTrack.info.trackId) {
                if (localTrack.info.kind !== 'audio') {
                // 调用 Track 对象的 play 方法在这个元素下播放视频轨
                  localTrack.play(localElement, true);
                }
                this.publishTrackList.push(localTrack)
              }
            }
          });
          console.log("publish success!");
        })
      },
      // 这里的参数 myRoom 是指刚刚加入房间时初始化的 Session 对象, 同上
      autoSubscribe() {
        const trackInfoList = this.myRoom.trackInfoList;
        console.log("远程视频流", trackInfoList);
        this.subscribe(trackInfoList)
        // 添加事件监听，当房间中出现新的 Track 时就会触发，参数是 trackInfo 列表
        this.myRoom.on("track-add", (tracks) => {
          console.log('新的tack', tracks)
          this.subscribe(tracks);
        });
      },
      subscribe(trackInfoList) {
        if (trackInfoList == [] || trackInfoList.length == 0) {
          return;
        }
        // 通过传入 trackId 调用订阅方法发起订阅，成功会返回相应的 Track 对象，也就是远端的 Track 列表了
        this.myRoom.subscribe(trackInfoList.map(info => info.trackId)).then((remoteTracks) => {
          // 选择页面上的一个元素作为父元素，播放远端的音视频轨
          const remoteElement = document.getElementById("remote");
          // 遍历返回的远端 Track，调用 play 方法完成在页面上的播放
          for (const remoteTrack of remoteTracks) {
            if (remoteTrack.info.kind) {
              this.remoteTrackList.push(remoteTrack)
              remoteTrack.play(remoteElement);
            }
          }
        });
      },
      tryCall(uname) {
        this.roomname = this.uname
        this.joinRoom().then(() => {
          this.publish()
          this.autoSubscribe()
        });
        this.target = uname
        this.websock.send(JSON.stringify({
          "cmd": "user",
          "toUser": uname,
          "type": "tryCall",
          "data": {
            "roomname": this.roomname
          }
        }))
      },
      //  websocket
      initWebSocket: function () {
        // WebSocket与普通的请求所用协议有所不同，ws等同于http，wss等同于https
        var url =
          window._CONFIG['qMediaSockURL'].replace('https://', 'wss://').replace('http://', 'ws://') +
          '/websocket/' + this.uname
        this.websock = new WebSocket(url)
        this.websock.onopen = this.websocketonopen
        this.websock.onerror = this.websocketonerror
        this.websock.onmessage = this.websocketonmessage
        this.websock.onclose = this.websocketclose
      },
      websocketonopen() {
        this.out = setInterval(() => {
          this.websock.send(JSON.stringify({
            "cmd": "user",
            "toUser": this.uname,
            "type": "heart-beat"
          }));
        }, 25000);
        console.log('WebSocket连接成功')
      },
      websocketonerror(e) {
        clearTimeout(this.out)
        if (this.retryNum++ > 0) {
          //重试
          setTimeout(this.initWebSocket, 1000);
        } else {
          alert('网络通信失败!')
          this.quit()
        }
        console.log('WebSocket连接发生错误',e)
      },
      websocketonmessage(e) {
        var data = eval('(' + e.data + ')')
        if (data.type === 'userOnlineList') {
          var newArr = [];
          for (var i = 0; i < data.data.length; i++) {
            if (data.data[i].uname !== this.uname) {
              newArr.push(data.data[i])
            } else {
              newArr.unshift(data.data[i])
            }
          };
          this.userList = newArr
        } else if (data.type === 'tryCall') {
          Dialog.confirm({
            title: "请求视频通讯",
            message: data.from.uname + "请求和您通话,是否连接？",
            confirmButtonText: "接受",
            cancelButtonText: "拒绝"
          })
            .then(() => {
              console.log('接受')
              this.target = data.from.uname
              this.websock.send(JSON.stringify({
                "cmd": "user",
                "toUser": data.from.uname,
                "type": "accpet"
              }));
              this.roomname = data.data.roomname
              this.joinRoom().then(() => {
                this.publish()
                this.autoSubscribe()
              });
            })
            .catch(() => {
              this.websock.send(JSON.stringify({
                "cmd": "user",
                "toUser": data.from.uname,
                "type": "refuse"
              }));
            });
        } else if (data.type === 'refuse') {
          this.leave()
          alert('对方拒绝通话')
        } else if (data.type === 'leave') {
          this.leave()
          alert('对方已挂断')
        }
      },
      hangup() {
        this.leave()
        this.websock.send(JSON.stringify({
          "cmd": "user",
          "toUser": this.target,
          "type": "leave"
        }));
      },
      leave() {
        this.myRoom.leaveRoom();
        this.publishTrackList.forEach(t => {
          t && t.release()
        });
        this.publishTrackList = [];
        this.remoteTrackList = []

        if (this.localTrackList.length > 0) {
          this.localTrackList.forEach(t => t && t.release());
          this.localTrackList = []
        }
        this.roomtoken = '';
        this.roomname = '';

        this.aCallShow = false;
        this.isLinked = false;
      },

      websocketclose(e) {
        console.log('connection closed ')
      },
    },
  };

</script>
<style scoped>
  .box {
    height: 88vh;
    background-color: #fff;
  }

  .a_call {
    width: 20px;
    margin-left: 5px;
  }

  .title {
    font-size: 16px;
    color: #333;
  }

  /*通话*/
  .videoEl {
    width: 100%;
    height: 100%;
  }

  .goback {
    position: absolute;
    left: 10px;
    top: 30px;
    font-size: 16px;
    color: #fff;
  }

  .splitter {
    position: absolute;
    right: 5px;
    top: 40px;
    width: 105px;
    height: 130px;
    border-radius: 4px;
    overflow: hidden;
    background: linear-gradient(#aaaeb4, #636b76);
    border: 1px solid #636b76;
    box-shadow: 0px 0px 5px #636b76;
  }

  .aCallBtnBox {
    position: absolute;
    left: 50%;
    bottom: 45px;
    transform: translate(-50%, 0);
  }

  .aCallBtn,
  .aCallBtn1 {
    height: 55px !important;
    width: 160px !important;
  }

  .aCallBtn {
    border-color: #ec2f2f;
    background-color: #ec2f2f;
  }

  .aCallBtn1 {
    border-color: #0fd400;
    background-color: #0fd400;
    color: #fff;
  }

  .aCallBtn1:hover {
    border-color: #75ef70;
    background-color: #75ef70;
    color: #fff;
  }

  .aCallBtn img,
  .aCallBtn1 img {
    width: 24px;
    margin-right: 10px;
  }

  /*建立连接前*/
  .largeIcon {
    position: absolute;
    text-align: center;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }

  .largeIcon img {
    margin-bottom: 10px;
    width: 40%;
  }

  .largeIcon div {
    color: #fff;
    letter-spacing: 2px;
  }

  .largeIcon div img {
    display: inline-block;
    vertical-align: middle;
    width: 26%;
  }

  .splitter .largeIcon img {
    width: 70%;
  }
</style>
<style>
  .box ul {
    background-color: #fff;
  }

  .box .ant-list {
    padding: 8px 24px;
    margin-top: 80px;
  }

  .btnGroup {
    display: flex;
    justify-content: space-between;
  }

  .btnGroup .aCallBtn1 {
    margin-right: 20px;
  }
</style>