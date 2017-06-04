/**
 * Created by Hins on 2017/6/4.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Hins on 2017/6/4.
 */
public class MyScse {
    private static final String SERVER = "http://class.sise.com.cn:7001";
    private static final String LOGINURL = "/sise/login.jsp";
    private static final String ACTION = "/sise/login_check_login.jsp";
    private static final String MAINURL = "/sise/module/student_states/student_select_class/main.jsp";
    private String SchedularUrl = "";
    private String cookies = "";
    private String username = "1440125128";
    private String password = "zhong1520gg";
    private String hidden = "";

    public void getCookies(HttpURLConnection connection) {
        if (cookies.equals("")&&cookies.length()<=0){
            String key = null;
            //遍历报头获取Cookie
            for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
                key = connection.getHeaderFieldKey(i);
                if (key.equalsIgnoreCase("set-cookie")) {
                    String cookie = connection.getHeaderField(key);
                    cookies += cookie;
                }
            }
        }
    }

    public void getHidden() {
        String html=getContent(LOGINURL);
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByAttributeValue("type", "hidden");
        for (int i = 0; i < elements.size(); i++) {
            Element child = elements.get(i);
            hidden += child.attr("name") + "=" + child.attr("value") + "&";
        }
        //System.out.println(hidden);
    }

    public void login() {
        try {
            URL url = new URL(SERVER + ACTION);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookies);
            connection.setDoOutput(true);//设置
            String post = hidden + "username=" + username + "&password=" + password;
            //System.out.println(post);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(post.getBytes());
            outputStream.flush();
            outputStream.close();
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "gb2312");
            BufferedReader buffer = new BufferedReader(reader);
            while ((line = buffer.readLine()) != null) {
                //System.out.println(line);
                stringBuffer.append(line);
            }
            if (stringBuffer.toString().contains("index.jsp")) {//<script>top.location.href='/sise/index.jsp'</script>
                String html = getContent(MAINURL);
                //System.out.println(html);
                Document document = Jsoup.parse(html);
                Elements elements = document.getElementsByAttribute("onclick");
                //System.out.println(elements);
                System.out.println("==============================链接==============================");
                for (int i = 0; i < elements.size(); i++) {
                    Element child = elements.get(i);
                    String title = child.getElementsByTag("strong").text();
                    String attr = child.attr("onclick");
                    String link = attr.substring(attr.indexOf("'") + 1, attr.lastIndexOf("'"));
                    link = link.replace("../../../../..", "");
                    System.out.println(title+"==>"+SERVER+link);
                    if (title.equals("课程表")) {
                        SchedularUrl = link;
                        //System.out.println(SchedularUrl);
                    }
                }
                System.out.println("==============================链接==============================");
                System.out.println("");
                //System.out.println(SERVER+SchedularUrl);
                html=getContent(SchedularUrl);
                //System.out.println(html);
                document=Jsoup.parse(html);
                //elements=document.getElementsByTag("form");
                Elements spans=document.getElementsByClass("style17");
                System.out.println(spans.text());
                spans=document.getElementsByClass("style16");
                for (int i = 0; i < spans.size(); i++) {
                    String text=spans.get(i).text();
                    text=text.replace("&nbsp;","    ");
                    System.out.println(text);
                }
                Elements tbodys=document.getElementsByTag("tbody");
                Elements trs=tbodys.last().children();
                //System.out.println(tr);
                for (int i = 0; i < trs.size(); i++) {
                    Elements tds=trs.get(i).children();
                    for (int j = 0; j < tds.size(); j++) {
                        System.out.print(tds.get(j).text());
                    }
                    System.out.println("");
                }
            } else {
                throw new Exception("登录失败");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getContent(String paramUrl) {
        try {
            URL url = new URL(SERVER + paramUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", cookies);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                getCookies(connection);
                InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "gb2312");
                BufferedReader buffer = new BufferedReader(reader);
                String line = null;
                StringBuffer html = new StringBuffer();
                while ((line = buffer.readLine()) != null) {
                    html.append(line);
                }
                return html.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        MyScse myScse = new MyScse();
        myScse.getHidden();
        myScse.login();
    }

}

