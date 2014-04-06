package com.hypersocket.client.redirect;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Structure;

public class MacOSXDriverControlSocket {

	public static final String LIBRARY = "c";

	public static final int SOCK_DGRAM = 2;
	public static final int SYSPROTO_CONTROL = 2;
	public static final int AF_SYSTEM = 32;
	
	public static final String BUNDLEID = "com.javassh.hermitnke";
	public static final String KERNCTLID = "com.javassh.hermitkernctl";
	
	// struct sockaddr_ctl {
	// u_char sc_len; /* depends on size of bundle ID string */
	// u_char sc_family; /* AF_SYSTEM */
	// u_int16_t ss_sysaddr; /* AF_SYS_KERNCONTROL */
	// u_int32_t sc_id; /* Controller unique identifier */
	// u_int32_t sc_unit; /* Developer private unit number */
	// u_int32_t sc_reserved[5];
	// };

	protected static class SockAddrCtl extends Structure {

		public final static byte[] ZERO_BYTE = new byte[] { 0 };

		public byte sc_len;
		public byte sc_family;
		public short ss_sysaddr;
		public int sc_id;
		public int sc_unit;
		public int sc_reserved[];

		@SuppressWarnings("rawtypes")
		@Override
		protected List getFieldOrder() {
			return Arrays.asList(new String[] { "sc_len", "sc_family", "ss_sysaddr", "sc_id", "sc_unit", "sc_reserved" });
		}
	}

//	struct ctl_info {
//	    u_int32_t	ctl_id; 				/* Kernel Controller ID  */
//	    char	ctl_name[MAX_KCTL_NAME];		/* Kernel Controller Name (a C string) */
//	};
//	
	
//	CTLIOCGINFO _IOWR('N', 3, struct ctl_info)
	
	public static int IOCPARM_MASK = 0x1fff;
	public static int IOC_OUT = 0x40000000;
	public static int IOC_IN = 0x80000000;
	public static int IOC(int inout, int group, int num, int len) {
		return inout | ((len & IOCPARM_MASK) << 16) | ((group) << 8) | num;
	}
	
	public static int CTLIOCINFO(CtlInfo ctlinfo) {
		return IOC(IOC_IN|IOC_OUT, 'N', 3, ctlinfo.size());
	}
	protected static class CtlInfo extends Structure {
		
		public int ctl_id;
		public byte[] ctl_name = new byte[96];
		
		@SuppressWarnings("rawtypes")
		@Override
		protected List getFieldOrder() {
			return Arrays.asList(new String[] { "ctl_id", "ctl_name" });
		}
		
	}
	
//	#define	IOCPARM_MAX	NBPG		/* max size of ioctl, mult. of NBPG */
//	#define	IOC_VOID	0x20000000	/* no parameters */
//	#define	IOC_OUT		0x40000000	/* copy out parameters */
//	#define	IOC_IN		0x80000000	/* copy in parameters */
//	#define	IOC_INOUT	(IOC_IN|IOC_OUT)
//	#define	IOC_DIRMASK	0xe0000000	/* mask for IN/OUT/VOID */
//
//	#define _IOC(inout,group,num,len) \
//		(inout | ((len & IOCPARM_MASK) << 16) | ((group) << 8) | (num))
//	#define	_IO(g,n)	_IOC(IOC_VOID,	(g), (n), 0)
//	#define	_IOR(g,n,t)	_IOC(IOC_OUT,	(g), (n), sizeof(t))
//	#define	_IOW(g,n,t)	_IOC(IOC_IN,	(g), (n), sizeof(t))
//	/* this should be _IORW, but stdio got there first */
//	#define	_IOWR(g,n,t)	_IOC(IOC_INOUT,	(g), (n), sizeof(t))

	
	
	protected interface CLibrary extends Library {
		public int socket(int domain, int type, int protocol);

		public int connect(int sockfd, SockAddrCtl sockaddr, int addrlen);

		public int read(int fd, ByteBuffer buffer, int count);

		public int write(int fd, ByteBuffer buffer, int count);

		public int close(int fd);

		public String strerror(int errno);
		
		public int ioctl(int fd, int request, CtlInfo ctlinfo) throws LastErrorException;
	}

	protected static CLibrary libraryInstance = null;

	protected synchronized static void loadLibrary() throws SocketException {
		if (libraryInstance == null) {
			if (!Platform.isMac()) {
				throw new SocketException(
						"loadLibrary(): This redirect control socket will only work with the Mac OSX redirect driver");
			}

			libraryInstance = (CLibrary) Native.loadLibrary(LIBRARY,
					CLibrary.class);
		}
	}
	
	public static void main(String[] args) {
		
		try {
			
			Process p = Runtime.getRuntime().exec("whoami");
			int b;
			byte[] buf = new byte[1024];
			while((b = p.getInputStream().read(buf))>-1) {
				System.out.write(buf, 0, b);
			}
			System.out.flush();
			
			loadLibrary();
			
			int g_sock = libraryInstance.socket(AF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL);
			if(g_sock < 0) {
				System.out.println("Could not connect control socket");
				return;
			}
			
			CtlInfo ctlinfo = new CtlInfo();
			ctlinfo.ctl_id = 0;
			System.arraycopy(KERNCTLID.getBytes(), 0, ctlinfo.ctl_name, 0, KERNCTLID.getBytes().length);

//			System.out.println(CTLIOCINFO(ctlinfo));
//			System.out.println(ctlinfo.size());
			if(libraryInstance.ioctl(g_sock, CTLIOCINFO(ctlinfo), ctlinfo) == -1) {
				System.out.println("ioctl failed");
			}
			
			SockAddrCtl sc = new SockAddrCtl();
			sc.sc_len = 0;
			sc.sc_family = AF_SYSTEM;
			sc.ss_sysaddr = SYSPROTO_CONTROL;
			sc.sc_id = ctlinfo.ctl_id;
			sc.sc_unit = 0;
			sc.sc_reserved = new int[5];
			sc.sc_len = (byte) sc.size();
			
			int con = libraryInstance.connect(g_sock, sc, sc.size());
			
			System.out.println("connect returned " + con);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		g_sock = socket(PF_SYSTEM, SOCK_DGRAM, SYSPROTO_CONTROL);
//		if (g_sock < 0) 
//			return -1;
//		
//	    bzero(&ctl_info, sizeof(struct ctl_info));
//	    strcpy(ctl_info.ctl_name, KERNCTLID);
//	    
//	    if (ioctl(g_sock, CTLIOCGINFO, &ctl_info) == -1) 
//			return -1;
//	    
//		bzero(&sc, sizeof(struct sockaddr_ctl));
//		sc.sc_len = sizeof(struct sockaddr_ctl);
//		sc.sc_family = AF_SYSTEM;
//		sc.ss_sysaddr = SYSPROTO_CONTROL;
//		sc.sc_id = ctl_info.ctl_id;
//		sc.sc_unit = 0;
//	    
//		if (connect(g_sock, (struct sockaddr *)&sc, sizeof(struct sockaddr_ctl))) 
//	        return -1;
//
//	    return 0;

		
		
	}
}
