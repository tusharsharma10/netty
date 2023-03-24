package server.http;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.protocol.server.NettyServer;

public class HttpServer {

  private int port;
  private static Logger logger = LoggerFactory.getLogger(HttpServer.class);

  public HttpServer(int port) {
    this.port = port;
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    new HttpServer(port).run();
  }

  public void run() throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
              ChannelPipeline p = ch.pipeline();
              p.addLast(new HttpRequestDecoder());
              p.addLast(new HttpResponseEncoder());
              p.addLast(new CustomHttpServerHandler());
            }
          });

      ChannelFuture f = b.bind(port).sync();
      f.channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}