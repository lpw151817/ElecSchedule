package nercms.schedule.view;

/**
 * RecordStrategy ¼�����Խӿ�
 * @author acer
 */
public interface RecordStrategy {
 
    /**
     * ���������¼��׼������������¼���ļ�����
     */
    public void ready();
    /**
     * ��ʼ¼��
     */
    public void start();
    /**
     * ¼������
     */
    public void stop();
 
    /**
     * ¼��ʧ��ʱɾ��ԭ���ľ��ļ�
     */
    public void deleteOldFile();
 
    /**
     * ��ȡ¼�������Ĵ�С
     * @return 
     */
    public double getAmplitude();
 
    /**
     * ����¼���ļ�����·��
     * @return
     */
    public String getFilePath();
}
