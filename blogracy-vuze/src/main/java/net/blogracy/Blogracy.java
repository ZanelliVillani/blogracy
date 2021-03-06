/**
 * 
 */
package net.blogracy;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import net.blogracy.logging.Logger;
import net.blogracy.services.ChatService;
import net.blogracy.services.DownloadService;
import net.blogracy.services.LookupService;
import net.blogracy.services.SeedService;
import net.blogracy.services.StoreService;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginManager;

import com.aelitis.azureus.plugins.chat.ChatPlugin;

/**
 * @author mic
 * 
 */
public class Blogracy implements Plugin {

    private static BrokerService broker;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private StoreService storeService;
    private LookupService lookupService;
    private SeedService seedService;
    private DownloadService downloadService;
    private ChatService chatService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gudy.azureus2.plugins.Plugin#initialize(org.gudy.azureus2.plugins
     * .PluginInterface)
     */
    @Override
    public void initialize(PluginInterface vuze) throws PluginException {
        createQueues(vuze);
    }

    void createQueues(final PluginInterface vuze) {
        String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
        try {
            connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();

            storeService = new StoreService(connection, vuze);
            lookupService = new LookupService(connection, vuze);
            seedService = new SeedService(connection, vuze);
            downloadService = new DownloadService(connection, vuze);
            
            chatService = new ChatService(connection, vuze);
            Logger.info("Blogracy Vuze plugin has started correctly");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            broker = new BrokerService();
            broker.setBrokerName("blogracy");
            broker.addConnector(ActiveMQConnectionFactory.DEFAULT_BROKER_BIND_URL);
            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
        	(new java.io.File("plugins/chat")).mkdirs();
        	Properties chatProp = new Properties();
        	chatProp.load(Blogracy.class.getClassLoader().getResourceAsStream("plugins/chat/plugin.properties"));
        	chatProp.store(new FileWriter("plugins/chat/plugin.properties"), "Chat plugin");
        	(new java.io.File("plugins/blogracy")).mkdirs();
        	Properties blogracyProp = new Properties();
        	blogracyProp.load(Blogracy.class.getClassLoader().getResourceAsStream("plugins/blogracy/plugin.properties"));
        	blogracyProp.store(new FileWriter("plugins/blogracy/plugin.properties"), "Blogracy plugin");
        } catch (Exception e) {
        	e.printStackTrace();
        }

        List<String> argList = new ArrayList<String>();
        argList.addAll(Arrays.asList(args));
        argList.add("--ui=console");
        org.gudy.azureus2.ui.common.Main.main(argList.toArray(args));
        //PluginManager.startAzureus(PluginManager.UI_NONE, new java.util.Properties());
    }
}
