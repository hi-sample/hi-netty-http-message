package net.hifor.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

/**
 * @author IKin <br/>
 * @description <br/>
 * @create 2024/6/28 08:50 <br/>
 * 运行命令：
 *     java -jar hi-netty-http-1.0-SNAPSHOT.jar 8080
 */
public class HttpServerApplication {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(65536),
                                    new SimpleChannelInboundHandler<FullHttpRequest>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
                                            System.out.println("\n>>>>>>>>>> HTTP Request >>>>>>>>>>");
                                            System.out.println(req.method() + " " + req.uri() + " " + req.protocolVersion());
                                            for (CharSequence name : req.headers().names()) {
                                                System.out.println(name + ": " + req.headers().get(name));
                                            }
                                            System.out.println();
                                            if (req.content().isReadable()) {
                                                System.out.println(req.content().toString(Charset.forName("UTF-8")));
                                            }
                                            System.out.println("<<<<<<<<<<< End  Request <<<<<<<<<<<\n");

                                            FullHttpResponse response = new DefaultFullHttpResponse(
                                                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                                            response.content().writeBytes("{\"code\":0,\"msg\":\"ok\"}".getBytes());
                                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                        }
                                    }
                            );
                        }
                    });
            int port = 80;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            ChannelFuture f = b.bind(port).sync();
            System.out.println("HTTP server started at http://localhost:" + port + '/');
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
