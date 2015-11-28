package com.nercms;

public interface ICallBacks {

	/**
	 * ��Ϣ����ص�
	 * 
	 * @param client_id
	 * @param tag
	 *            ��������(Topic)
	 * @param msg_len
	 *            ��Ϣ(message)�ĳ���
	 * @param msg
	 *            ��Ϣ����
	 * @param msg_qos
	 *            qos
	 * @param duplicate_count
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 */
	public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg, int msg_qos,
			int duplicate_count);

	public void onDeliveryCompleteFunc(String client_id, int tocken);

	/**
	 * �Ͽ�����
	 * 
	 * @param client_id
	 *            ���ĵ�clientid
	 * @param cause
	 *            �Ͽ�ԭ��
	 */
	public void onConnectionLostFunc(String client_id, String cause);

}
