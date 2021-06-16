<template>
  <div>
    <div class="panel">
      <a-form class="form" :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 10 }" @submit="handleSubmit">
        <a-form-item label="账号">
          <a-input v-decorator="['uname', { rules: [{ required: true,pattern:'^[a-zA-Z0-9_-]{3,64}$', message: '请输入合法账号!' }] }]" />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12 }">
          <a-button type="primary" html-type="submit">
            进入
          </a-button>
        </a-form-item>
      </a-form>

    </div>
  </div>
</template>
<script>
  import Vue from 'vue'
  export default {
    name: "index",
    data() {
      return {
        uname: "",
        form: this.$form.createForm(this),
      };
    },
    created(){
      let uname = Vue.ls.get("uname");
      if(uname){
        this.$router.push("/room")
      }
    },
    methods: {
      handleSubmit(e) {
        e.preventDefault();
        this.form.validateFields((err, values) => {
          if (!err) {
            //30分钟
            Vue.ls.set("uname",values.uname,30*60*1000)
            this.$router.push("/room")
          }
        });
      },
    },
  };
</script>
<style scoped>
  .panel {
    text-align: center;
    margin: 50px auto;
    border: 1px solid #000;
    width: 700px;
    height: 300px
  }
  .form{
    text-align: center;
    margin: 50px auto;
  }
</style>