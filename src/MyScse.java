import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Hins on 2017/6/4.
 * 思路：1.使用HttpURLConnection模拟登陆信息系统，通过Cookie中的JSESSIONID(Tomcat中功能类似于session)确认身份，
 * 2.再使用JSoup解析出下一步的连接或想要的信息
 * 3.重复1、2步，直到获取到最终的信息
 */
public class MyScse {

    private static final String SERVER = "http://class.sise.com.cn:7001";//服务器域名
    private static final String LOGINURL = "/sise/login.jsp";//登陆入口地址
    private static final String ACTION = "/sise/login_check_login.jsp";//提交学号和密码的action地址
    private static final String MAINURL = "/sise/module/student_states/student_select_class/main.jsp";//主页的地址
    private String SchedularUrl = "";//课程表地址，每个同学的连接都不一样，需要同台爬去
    private String cookies = "";//JSESSIONID
    private String username = "";//学号
    private String password = "";//密码
    private String hidden = "";//登陆表单隐藏域

    /**
     * 获取服务器的cookie
     * @param connection 请求的connection
     */
    public void getCookies(HttpURLConnection connection) {
        //判断cookies是否为空
        if (isNullOrBlank(cookies)){
            String key = null;
            //遍历response报头获取Cookie
            for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
                key = connection.getHeaderFieldKey(i);
                if (key.equalsIgnoreCase("set-cookie")) {
                    String cookie = connection.getHeaderField(key);
                    cookies += cookie;
                }
            }
            //System.out.println(cookies);
        }
    }

    /**
     * 获取登陆表单的隐藏域
     */
    public void getHidden() {
        //爬去登陆页面
        String html=getContent(LOGINURL);
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByAttributeValue("type", "hidden");
        //因为使用URLConnection模拟登陆，直接把所有参数以"&"隔开，一次过输出到action
        for (int i = 0; i < elements.size(); i++) {
            Element child = elements.get(i);
            hidden += child.attr("name") + "=" + child.attr("value") + "&";
        }
        //System.out.println(hidden);
    }

    /**
     * 模拟登陆
     */
    public void login() {
        try {
            URL url = new URL(SERVER + ACTION);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            //把之前获取到的JSESSIONID放回request报头
            connection.setRequestProperty("Cookie", cookies);
            connection.setDoOutput(true);//必须设置
            //把表单参数集合好
            String post = hidden + "username=" + username + "&password=" + password;
            //使用输出流写入参数
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(post.getBytes());
            outputStream.flush();
            outputStream.close();
            //获取response的网页
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "gb2312");
            BufferedReader buffer = new BufferedReader(reader);
            while ((line = buffer.readLine()) != null) {
                //System.out.println(line);
                stringBuffer.append(line);
            }
            buffer.close();
            reader.close();
            connection.disconnect();
            //判断是否登陆成功
            //当html的值为 <script>top.location.href='/sise/index.jsp'</script>时,说明登录成功了
            //此时如果是浏览器登录就会自动跳转到http://class.sise.com.cn:7001/sise/index.jsp界面,只是爬虫是不会自己跳转的
            if (stringBuffer.toString().contains("index.jsp")) {
                String html = getContent(MAINURL);
                //System.out.println(html);
                Document document = Jsoup.parse(html);
                Elements elements = document.getElementsByAttribute("onclick");
                //System.out.println(elements);
                //输出主页中的所有连接
                System.out.println("==============================链接==============================");
                for (int i = 0; i < elements.size(); i++) {
                    Element child = elements.get(i);
                    String title = child.getElementsByTag("strong").text();
                    //连接保存在onclick属性里面
                    String attr = child.attr("onclick");
                    String link = attr.substring(attr.indexOf("'") + 1, attr.lastIndexOf("'"));
                    //有的连接会有../..，我们需要替换掉
                    link = link.replace("../../../../..", "");
                    System.out.println(title+"==>"+SERVER+link);
                    //动态获取课程表的连接
                    if (title.equals("课程表")) {
                        SchedularUrl = link;
                        //System.out.println(SchedularUrl);
                    }
                }
                System.out.println("==============================链接==============================");
                System.out.println("");

                //获取课程表的网页
                html=getContent(SchedularUrl);
                document=Jsoup.parse(html);

                Elements spans=document.getElementsByClass("style17");
                System.out.println(spans.text());
                //个人信息
                spans=document.getElementsByClass("style16");
                for (int i = 0; i < spans.size(); i++) {
                    String text=spans.get(i).text();
                    //text=text.replace("&nbsp;","\t");
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
                throw new Exception("密码错误");
            }
        }  catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * @param paramUrl 链接
     * @return 爬取到的网页
     */
    public String getContent(String paramUrl) {
        try {
            URL url = new URL(SERVER + paramUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //每次爬取都要放入cookie以保持登陆状态
            connection.setRequestProperty("Cookie", cookies);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                //获取response报头中的cookie
                getCookies(connection);
                InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "gb2312");
                BufferedReader buffer = new BufferedReader(reader);
                String line = null;
                StringBuffer html = new StringBuffer();
                while ((line = buffer.readLine()) != null) {
                    html.append(line);
                }
                buffer.close();
                reader.close();
                connection.disconnect();
                return html.toString();
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return null;
    }

    /**
     * 获取程序参数中的用户名和密码
     * @param args 程序参数
     */
    public void setUser(String[] args) {
        try{
            if (isNullOrBlank(username) || isNullOrBlank(password)) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-u")) {
                        username = args[i + 1].trim();
                        if (isNullOrBlank(username)) {
                            throw new Exception("请在关键字 -u 后面输入用户名。");
                        }
                    }
                    if (args[i].equals("-p")) {
                        password = args[i + 1].trim();
                        if (isNullOrBlank(password)) {
                            throw new Exception("请在关键字 -p 后面输入密码。");
                        }
                    }
                }
                if (isNullOrBlank(username) || isNullOrBlank(password)) {
                    throw new Exception("用户名和密码都不能为空。");
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }

    /**
     * 判断目标字符串是否null或者空格
     * @param str 字符串
     */
    public boolean isNullOrBlank(String str) {
        if (str == null || "".equals(str.trim()) || str.isEmpty())
            return true;
        return false;
    }

    public static void main(String[] args) {
        MyScse myScse = new MyScse();
        myScse.setUser(args);
        myScse.getHidden();
        myScse.login();
    }
}

