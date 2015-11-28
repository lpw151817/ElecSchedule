package com.nercms;

public interface ICallBacks {

	/**
	 * 消息到达回调
	 * 
	 * @param client_id
	 * @param tag
	 *            订阅主题(Topic)
	 * @param msg_len
	 *            消息(message)的长度
	 * @param msg
	 *            消息内容
	 * @param msg_qos
	 *            qos
	 * @param duplicate_count
	 * @return 成功返回0，失败返回-1
	 */
	public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg, int msg_qos,
			int duplicate_count);

	public void onDeliveryCompleteFunc(String client_id, int tocken);

	/**
	 * 断开连接
	 * 
	 * @param client_id
	 *            订阅的clientid
	 * @param cause
	 *            断开原因
	 */
	public void onConnectionLostFunc(String client_id, String cause);

}
