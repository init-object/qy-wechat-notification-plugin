package org.jenkinsci.plugins.qywechat.dto;

import org.jenkinsci.plugins.qywechat.NotificationUtil;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;
import hudson.model.Result;
import hudson.model.Run;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束构建的通知信息
 * @author jiaju
 */
public class BuildOverInfo {

    /**
     * 使用时间，毫秒
     */
    private String useTimeString = "";

    /**
     * 本次构建控制台地址
     */
    private String consoleUrl;

    /**
     * 本次构建Blue Ocean控制台地址
     */
    private String buleOceanConsoleUrl;

    /**
     * 工程名称
     */
    private String projectName;

    /**
     * 环境名称
     */
    private String topicName = "";

    /**
     * 执行结果
     */
    private Result result;
    
    /**
    * 更多自定消息
    */
   private String moreInfo = "";

    public BuildOverInfo(String projectName, Run<?, ?> run, NotificationConfig config){
        //使用时间
        this.useTimeString = run.getTimestampString();
        //控制台地址
        StringBuilder urlBuilder = new StringBuilder();
        StringBuilder blueOceanUrlBuilder = new StringBuilder();
        
        String jenkinsUrl = NotificationUtil.getJenkinsUrl();
        if(StringUtils.isNotEmpty(jenkinsUrl)){
            String buildUrl = run.getUrl();
            urlBuilder.append(jenkinsUrl);
            blueOceanUrlBuilder.append(jenkinsUrl);
            if(!jenkinsUrl.endsWith("/")){
                urlBuilder.append("/");
                blueOceanUrlBuilder.append("/");
            }
            blueOceanUrlBuilder.append("blue/organizations/jenkins/");
            blueOceanUrlBuilder.append(projectName + "/detail/" + projectName + "/"+ run.getNumber()+"/");
            urlBuilder.append(buildUrl);
            if(!buildUrl.endsWith("/")){
                urlBuilder.append("/");
                blueOceanUrlBuilder.append("/");
            }
            urlBuilder.append("console");
            blueOceanUrlBuilder.append("pipeline/");
        }
        this.consoleUrl = urlBuilder.toString();
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
        //结果
        result = run.getResult();
    }

    public String toJSONString(){
         //组装内容
         Map<String, Object> result = new HashMap<String, Object>();
         StringBuilder title = new StringBuilder();
         if(StringUtils.isNotEmpty(topicName)){
            title.append(this.topicName);
         }
         title.append("【" + this.projectName + "】构建" + getStatus() + "\n");
         result.put("title", title.toString());
         StringBuilder content = new StringBuilder();

         content.append("构建用时：" +  this.useTimeString + "\n");
         if (StringUtils.isNotEmpty(moreInfo)){
            content.append("\n"+moreInfo+"\n\n");
        }

         if(StringUtils.isNotEmpty(this.consoleUrl)){
             content.append("[查看控制台]:\n" + this.consoleUrl + "\n");
             content.append("[查看BlueOcean控制台]:\n" + this.buleOceanConsoleUrl + "\n");
         }
 
         result.put("content", content.toString());

        String req = JSONObject.fromObject(result).toString();
        return req;
    }

    private String getStatus(){
        if(null != result && result.equals(Result.FAILURE)){
            return "失败!!!\uD83D\uDE2D";
        }else if(null != result && result.equals(Result.ABORTED)){
            return "中断!!\uD83D\uDE28";
        }else if(null != result && result.equals(Result.UNSTABLE)){
            return "异常!!\uD83D\uDE41";
        }else if(null != result && result.equals(Result.SUCCESS)){
            int max=successFaces.length-1,min=0;
            int ran = (int) (Math.random()*(max-min)+min);
            return "成功~" + successFaces[ran];
        }
        return "情况未知";
    }

    String []successFaces = {
            "\uD83D\uDE0A", "\uD83D\uDE04", "\uD83D\uDE0E", "\uD83D\uDC4C", "\uD83D\uDC4D", "(o´ω`o)و", "(๑•̀ㅂ•́)و✧"
    };


}
