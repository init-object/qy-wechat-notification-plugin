package org.jenkinsci.plugins.qywechat.dto;

import org.jenkinsci.plugins.qywechat.NotificationUtil;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开始构建的通知信息
 * @author jiaju
 */
public class BuildBeginInfo {

    /**
     * 请求参数
     */
    private Map params = new HashMap<String, Object>();

    /**
     * 预计时间，毫秒
     */
    private Long durationTime = 0L;

    /**
     * 本次构建控制台地址
     */
    private String consoleUrl;


    /**
     * 本次构建Blue Ocean控制台地址
     */
    private String buleOceanConsoleUrl;

    private boolean useBuleOceanConsole = false;
    

    /**
     * 工程名称
     */
    private String projectName;

    /**
     * 环境名称
     */
    private String topicName = "";

    /**
     * 更多自定消息
     */
    private String moreInfo = "";

    public BuildBeginInfo(String projectName, AbstractBuild<?, ?> build, NotificationConfig config){
        //获取请求参数
        List<ParametersAction> parameterList = build.getActions(ParametersAction.class);
        if(parameterList!=null && parameterList.size()>0){
            for(ParametersAction p : parameterList){
                for(ParameterValue pv : p.getParameters()){
                    this.params.put(pv.getName(), pv.getValue());
                }
            }
        }
        //预计时间
        if(build.getProject().getEstimatedDuration()>0){
            this.durationTime = build.getProject().getEstimatedDuration();
        }
        //控制台地址
        StringBuilder urlBuilder = new StringBuilder();
        StringBuilder blueOceanUrlBuilder = new StringBuilder();
        
        String jenkinsUrl = NotificationUtil.getJenkinsUrl();
        if(StringUtils.isNotEmpty(jenkinsUrl)){
            String buildUrl = build.getUrl();
            urlBuilder.append(jenkinsUrl);
            blueOceanUrlBuilder.append(jenkinsUrl);
            if(!jenkinsUrl.endsWith("/")){
                urlBuilder.append("/");
                blueOceanUrlBuilder.append("/");
            }
            blueOceanUrlBuilder.append("blue/organizations/jenkins/");
            blueOceanUrlBuilder.append(projectName + "/detail/" + projectName + "/"+ build.getNumber()+"/");
            urlBuilder.append(buildUrl);
            if(!buildUrl.endsWith("/")){
                urlBuilder.append("/");
                blueOceanUrlBuilder.append("/");
            }
            urlBuilder.append("console");
            blueOceanUrlBuilder.append("pipeline/");
        }
        this.consoleUrl = urlBuilder.toString();
        this.useBuleOceanConsole = config.useBuleOceanConsole;
        this.buleOceanConsoleUrl = blueOceanUrlBuilder.toString();
        //工程名称
        this.projectName = projectName;
        //环境名称
        if(config.topicName!=null){
            topicName = config.topicName;
        }
        if (StringUtils.isNotEmpty(config.moreInfo)){
            moreInfo = config.moreInfo;
        }
    }

    public String toJSONString(){
        //参数组装
        StringBuffer paramBuffer = new StringBuffer();
        params.forEach((key, val)->{
            paramBuffer.append(key);
            paramBuffer.append("=");
            paramBuffer.append(val);
            paramBuffer.append(", ");
        });
        if(paramBuffer.length()==0){
            paramBuffer.append("无");
        }else{
            paramBuffer.deleteCharAt(paramBuffer.length()-2);
        }

        //耗时预计
        String durationTimeStr = "无";
        if(durationTime>0){
            Long l = durationTime / (1000 * 60);
            durationTimeStr = l + "分钟";
        }

        //组装内容
        Map<String, Object> result = new HashMap<String, Object>();
        StringBuilder title = new StringBuilder();
        if(StringUtils.isNotEmpty(topicName)){
            title.append(this.topicName);
        }
        title.append("【" + this.projectName + "】开始构建\n");
        result.put("title", title.toString());
         StringBuilder content = new StringBuilder();
        content.append("构建参数：" + paramBuffer.toString() + "\n");
        content.append("预计用时：" +  durationTimeStr + "\n");
        if (StringUtils.isNotEmpty(moreInfo)){
            content.append("\n"+moreInfo+"\n\n");
        }
        if(StringUtils.isNotEmpty(this.consoleUrl)){
            if (this.useBuleOceanConsole) {
                content.append("[查看BlueOcean控制台]:\n" + this.buleOceanConsoleUrl + "\n");
            } else {
                content.append("[查看控制台]:\n " + this.consoleUrl + "\n");
            }
        }

        result.put("content", content.toString());

        String req = JSONObject.fromObject(result).toString();
        return req;
    }



}
