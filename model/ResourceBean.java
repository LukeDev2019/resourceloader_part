package com.detech.universalpay.resourceloader.model;

/**
 * Created by Luke O on 2018/2/8.
 * 资源文件属性
 */

public class ResourceBean <Self extends ResourceBean<Self>>{

    public static final String STATE_ACTIVE                 = "1";

    public static final String KEY_HEAD                     = "info";
    public static final String KEY_FORMAT                   = "fileType";
    public static final String KEY_TYPE                     = "type";
    public static final String KEY_VERSION_CODE             = "resource_version_id";
    public static final String KEY_DOWNLOAD_PATH            = "download_url";
    public static final String KEY_RESOURCE_ID              = "resource_id";
    public static final String KEY_RESOURCE_ORIGIN_NAME     = "resource_name";
    public static final String KEY_STATE                    = "resource_status";
    public static final String KEY_MD5                      = "md5";
    public static final String KEY_ZIPPED                   = "zipped";//用来标记已解压

    public static final int FROM_LOCAL              = 0;//更新信息来自本地
    public static final int FROM_SERVER             = 1;//更新信息来自服务器

    private String format = "";
    private String type = "";
    private String versionCode = "";
    private String downloadPath = "";
    private String localFilePath = "";//本地文件路径
    private String localFolderPath = "";//本地文件夹路径
    private String resourceId = "";
    private String resourceName = "";
    private String resourceOriginName = "";//上传时的文件名
    private String md5 = "";
    private String state = "";
    private boolean downloading;
    private boolean unZipping;//是否正在解压
    private boolean zipped;//是否解压完成
    private boolean loaded;//是否成功加载
    private int downloadTime;//下载次数
    private int checkDownloadTimeoutIndex;//检测下载超时的序号
    private int lastCurrentByte;//上一次下载的字节大小
    private int from;//更新来源
    private long downloadId;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(KEY_RESOURCE_ORIGIN_NAME + ": " + resourceOriginName + ", ")
                .append(KEY_FORMAT + ": " + format + ", ")
                .append(KEY_TYPE + ": " + type + ", ")
                .append(KEY_VERSION_CODE + ": " + versionCode + ", ")
                .append(KEY_DOWNLOAD_PATH + ": " + downloadPath + ", ")
                .append(KEY_RESOURCE_ID + " " + resourceId + ", ")
                .append(KEY_STATE + ": " + state + ", ")
                .append(KEY_MD5 + ": " + md5 + ", ")
                .append(KEY_ZIPPED + ": " + zipped);
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        ResourceBean bean = (ResourceBean) obj;
        boolean equal = bean.resourceId.equals(resourceId)&&
                bean.getMd5().equals(md5)&&
                bean.getResourceOriginName().equals(resourceOriginName)&&
                bean.getDownloadPath().equals(getDownloadPath())&&
                bean.getVersionCode().equals(getVersionCode());

        return equal;
    }

    public Self format(String format){
        this.format = format;
        return (Self) this;
    }

    public Self type(String type){
        this.type = type;
        return (Self) this;
    }

    public Self versionCode(String versionCode){
        this.versionCode = versionCode;
        return (Self) this;
    }

    public Self downloadPath(String downloadPath){
        this.downloadPath = downloadPath;
        return (Self) this;
    }

    public Self localFilePath(String localFilePath){
        this.localFilePath = localFilePath;
        return (Self) this;
    }

    public Self localFolderPath(String localFolderPath){
        this.localFolderPath = localFolderPath;
        return (Self) this;
    }

    public Self resourceId(String resourceId){
        this.resourceId = resourceId;
        return (Self) this;
    }

    public Self resourceName(String resourceName){
        this.resourceName = resourceName;
        return (Self) this;
    }

    public Self from(int from){
        this.from = from;
        return (Self) this;
    }

    public Self resourceOriginName(String resourceOriginName){
        this.resourceOriginName = resourceOriginName;
        return (Self) this;
    }

    public Self md5(String md5){
        this.md5 = md5;
        return (Self) this;
    }

    public Self state(String state){
        this.state = state;
        return (Self) this;
    }

    public Self downloading(boolean downloading){
        this.downloading = downloading;
        return (Self) this;
    }

    public Self zipped(boolean zipped){
        this.zipped = zipped;
        return (Self) this;
    }

    public Self unZipping(boolean unZipping){
        this.unZipping = unZipping;
        return (Self) this;
    }

    public Self loaded(boolean loaded){
        this.loaded = loaded;
        return (Self) this;
    }

    public Self downloadTime(int downloadTime){
        this.downloadTime = downloadTime;
        return (Self) this;
    }

    public Self checkDownloadTimeoutIndex(int checkDownloadTimeoutIndex){
        this.checkDownloadTimeoutIndex = checkDownloadTimeoutIndex;
        return (Self) this;
    }

    public Self lastCurrentByte(int lastCurrentByte){
        this.lastCurrentByte = lastCurrentByte;
        return (Self) this;
    }

    public Self downloadId(long downloadId){
        this.downloadId = downloadId;
        return (Self) this;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceOriginName(){
        return resourceOriginName;
    }

    public String getMd5() {
        return md5;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public String getState() {
        return state;
    }

    public int getDownloadTime() {
        return downloadTime;
    }

    public boolean isUnZipping() {
        return unZipping;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public int getCheckDownloadTimeoutIndex() {
        return checkDownloadTimeoutIndex;
    }

    public String getLocalFolderPath() {
        return localFolderPath;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isZipped() {
        return zipped;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public int getLastCurrentByte() {
        return lastCurrentByte;
    }

    public int getFrom() {
        return from;
    }
}
