package com.example.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.util.Log;

/**
 * @Describe 聊天类工具，定义了一些简单的关于聊天的小工具
 * @author lixiaosong
 * 
 */
public class ChatUtils {
	/**
	 * 获取用户的vcard信息
	 * 
	 * @param connection
	 *            连接
	 * @param user
	 *            用户信息
	 * @return
	 * @throws XMPPException
	 */
	public static VCard getUserVCard(XMPPConnection connection, String user)
			throws XMPPException {
		VCard vcard = new VCard();
		vcard.load(connection, user);
		return vcard;
	}

	public static List<String> searchUsers(XMPPConnection connection,
			String serverDomain, String userName) throws XMPPException {
		List<String> results = new ArrayList<String>();
		System.out.println("查询开始..............." + connection.getHost()
				+ connection.getServiceName());

		UserSearchManager usm = new UserSearchManager(connection);

		Form searchForm = usm.getSearchForm(serverDomain);
		Form answerForm = searchForm.createAnswerForm();
		answerForm.setAnswer("Username", true);
		answerForm.setAnswer("search", userName);
		ReportedData data = usm.getSearchResults(answerForm, serverDomain);

		Iterator<Row> it = data.getRows();
		Row row = null;
		while (it.hasNext()) {
			row = it.next();
			Log.v("lixiaosong", row.getValues("Username").next().toString());
			Log.v("lixiaosong", row.getValues("Name").next().toString());
			Log.v("lixiaosong", row.getValues("Email").next().toString());
		}

		return results;
	}

}
