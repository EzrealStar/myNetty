package rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 服务端实现
 */
public class Server {

	private int port;
	
	public Server(int port) {
		this.port = port;
	}
	
	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
																.channel(NioServerSocketChannel.class)
																.localAddress(port)
																.childHandler(new ChannelInitializer<SocketChannel>() {
																	@Override
																	protected void initChannel(SocketChannel ch)
																			throws Exception {
																		// TODO Auto-generated method stub
																		ChannelPipeline pipeline = ch.pipeline();  		                     
																		pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));  		                        
																		pipeline.addLast(new LengthFieldPrepender(4));  		                        
																		pipeline.addLast("encoder", new ObjectEncoder());    		                        
																		pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));  		                        
																		pipeline.addLast(new InvokerHandler());
																	}
																})
																.option(ChannelOption.SO_BACKLOG, 128)
																.childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = serverBootstrap.bind(port).sync();
			System.out.println("服务端开始监听端口：" + port);
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception{
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		
		new Server(port).start();
	}
}
