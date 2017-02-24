/**
 * Created by zhao on 2017/2/24.
 */

import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.NamingException;


public class Ldap {

    static public DirContext GetConnection() {
        DirContext ctx = null; // 这个就是LDAP的连接对象
        Hashtable<String, String> env = new Hashtable<String, String>(); // 定义一个哈希表来存连接信息

        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory"); // 记录工JNDI工厂
        env.put(Context.PROVIDER_URL, "ldap://192.168.0.1:389"); // LDAP的地址，要根据LDAP服务器IP进行修改，389是LDAP的默认端口
        env.put(Context.SECURITY_AUTHENTICATION, "simple"); // 这个是默认授权类型，一般不用改
        env.put(Context.SECURITY_PRINCIPAL, ""); // LDAP的账户名，一般是这样的格式：dc=cs,dc=hunan,dc=com
        // ，根据LDAP的配置情况来
        env.put(Context.SECURITY_CREDENTIALS, "123456"); // 对应上面账户的密码

        try {
            ctx = new InitialDirContext(env); // 初始化LDAP连接，连接成功后就可以用ctx来操作LDAP了
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return ctx;
    }

    static public boolean IsExist(String ldappath) {
        DirContext ctx = null; // 这是LDAP的连接对象，上篇文章中已经介绍过
        ctx = GetConnection(); // GetConnection其实就是用上篇文章中的代码所写的一个函数
        try {
            ctx.search(ldappath, null); // 测试检索一下
            return true; // 表示这个条目是存在的
        } catch (NamingException ex) {
            return false;
        }
    }

    static public NamingEnumeration<Object> SearchSubEntry(String ldappath,
                                                           String attrname, String attrval) {
        NamingEnumeration ret = null;
        DirContext ctx = null;
        ctx = GetConnection(); // 取连接对象
        if (ctx != null) {
            try {
                if (IsExist(ldappath)) // 先判断一下这个条目是不是存在
                {
                    // 设定搜索条件
                    Attributes matchAttrs = new BasicAttributes(true); // 建一个属性集合对象
                    if (attrname.compareTo("") != 0) // 如果传入了属性名称条件就加到属性集合里
                        matchAttrs.put(new BasicAttribute(attrname, attrval));
                    // 搜索符合条件的结果
                    NamingEnumeration answer = ctx.search(ldappath, matchAttrs);
                    ret = answer;
                }
            } catch (NamingException ex) {
                ret = null; // 出现异常时会返回null
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public static void main() {
        ArrayList<String> mylist = new ArrayList<String>();
        NamingEnumeration<Object> sret = null;
        sret = SearchSubEntry("dc=cs,dc=hunan,dc=com", "", "");
        try {
            while (sret.hasMore()) {
                SearchResult sr = (SearchResult) sret.next();
                mylist.add(sr.getName());
                Attributes s = sr.getAttributes();
                Attribute name = s.get("sAMAccountName");
                System.out.println(name.get());
                System.out.println(sr.getName());
            }
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}