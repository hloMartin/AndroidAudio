package com.miligames.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.miligames.utils.SDKLog;
import com.miligames.utils.UCommUtil;

import android.content.Context;

public class ConfigManager {

	private static final String TAG = ConfigManager.class.getName();

	private static final String MODULE_AUDIO = "miliaudio";

	private static final String CONFIG_NAME = "MiliConfig.xml";

	/** 存储游戏所有功能模块的节点名称 */
	private static final String NODE_MODULES = "modules";
	/** 存储游戏某个功能模块的节点名称 */
	private static final String NODE_MODULE = "module";

	private static final String ATTRS_MODULE_NAME = "name";

	private static final String ATTRS_AUDIO_UPLOAD_URL = "uploadUrl";

	private Context mContext;
	private static ConfigManager mInstance;

	private Element mRootElement;
	private String mAudioUploadUrl;

	private ConfigManager(Context context) {
		mContext = context;
		mRootElement = null;
		mAudioUploadUrl = null;
	}

	public static ConfigManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ConfigManager(context);
		}
		return mInstance;
	}

	/**
	 * 获取音频上传的服务器URL
	 * 
	 * @return String 上传后的录音路径
	 */
	public String getAudioUploadUrl() {
		if (UCommUtil.isStrEmpty(mAudioUploadUrl)) {
			mAudioUploadUrl = getAudioNameNodeMap().getNamedItem(ATTRS_AUDIO_UPLOAD_URL).getNodeValue();
		}
		return mAudioUploadUrl;
	}

	/**
	 * 返回音频模块的参数信息
	 * 
	 * @param nodeName nodeName
	 * @param attrsName attrsName
	 * @return String String
	 */
	private NamedNodeMap getAudioNameNodeMap() {
		if (mRootElement == null) {
			getRootElement();
		}
		NodeList nodes = mRootElement.getElementsByTagName(NODE_MODULES);
		if (nodes == null || nodes.getLength() <= 0) {
			SDKLog.w(TAG, "node is not exits, note name:" + NODE_MODULES);
			return null;
		}
		NodeList modleNodes = nodes.item(0).getChildNodes();
		for (int i = 0; i < modleNodes.getLength(); i++) {
			Node node = modleNodes.item(i);
			if (node.getNodeName().equals(NODE_MODULE)) {
				NamedNodeMap map = node.getAttributes();
				if (map.getNamedItem(ATTRS_MODULE_NAME).getNodeValue().equals(MODULE_AUDIO)) {
					return map;
				}
			}
		}
		return null;
	}

	/**
	 * 获取RootElement
	 */
	private void getRootElement() {
		try {
			InputStream mXmlResourceParser = mContext.getAssets().open(CONFIG_NAME);
			DocumentBuilder builder = null;
			DocumentBuilderFactory factory = null;
			factory = DocumentBuilderFactory.newInstance();
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				SDKLog.e(TAG, "readConfigXml factory创建失败", e1);
			}
			try {
				Document document = builder.parse(mXmlResourceParser);
				mRootElement = document.getDocumentElement();
			} catch (SAXException e) {
				SDKLog.e(TAG, "解析xml失败", e);
			}
		} catch (IOException e) {
			SDKLog.e(TAG, "解析xml失败", e);
		}
	}

}
