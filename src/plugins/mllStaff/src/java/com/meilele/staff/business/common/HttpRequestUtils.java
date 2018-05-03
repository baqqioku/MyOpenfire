package com.meilele.staff.business.common;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meilele.staff.business.staff.StaffInfo;

import net.sf.json.JSONObject;

public class HttpRequestUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);

	public static JSONObject httpPost(String url, String jsonParam) {
		return httpPost(url, jsonParam, false);
	}

	public static JSONObject httpPost(String url, String jsonParam, boolean noNeedResponse) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		JSONObject jsonResult = null;
		HttpPost method = new HttpPost(url);
		try {
			if (null != jsonParam) {
				StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			HttpResponse result = httpClient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			System.out.println(result.getStatusLine().getStatusCode());
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}
					jsonResult = JSONObject.fromObject(str);
				} catch (Exception e) {
					logger.error("post请求提交失败:" + url, e);
				}
			}
		} catch (IOException e) {
			logger.error("post请求提交失败:" + url, e);
		}
		return jsonResult;
	}

	public static JSONObject httpGet(String url) {
		JSONObject jsonResult = null;
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(response.getEntity());

				jsonResult = JSONObject.fromObject(strResult);
				url = URLDecoder.decode(url, "UTF-8");
			} else {
				logger.error("get请求提交失败:" + url);
			}
		} catch (IOException e) {
			logger.error("get请求提交失败:" + url, e);
		}
		return jsonResult;
	}
	
	public static Date getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            d = sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("日期转换异常：："+e.getMessage());
        }
        return d;
    }

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		String url = "http://192.168.1.11:80/plugins/mllstaff/staffinfo?action=add";
		StaffInfo staffInfo = new StaffInfo();
		staffInfo.setStoreUuid("123");
		staffInfo.setStoreName("美乐乐深圳南山5号店");
		staffInfo.setSysUuid("518f4df4-5b03-4ba9-a995-c197ad009cbfz");
		staffInfo.setPassword("mllm1121@666");
		staffInfo.setStaffName("路小测试");
		staffInfo.setStaffType("1");
		staffInfo.setStaffPhone("15502184238");
		staffInfo.setId(Integer.valueOf(10000004));
		staffInfo.setCodeImage(
				"https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQHv8DoAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2ZrUW5aS1hsNGpXYlNZNXRKMml4AAIEpwiXVgMEAAAAAA==");
		staffInfo.setCodeId("179");
		staffInfo.setRolUuid("1");
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(staffInfo));
		System.out.println(httpPost(url, mapper.writeValueAsString(staffInfo)));
	}
}
