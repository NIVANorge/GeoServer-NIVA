package niva.geoserver.process;

import org.geotools.process.factory.AnnotatedBeanProcessFactory;
import org.geotools.text.Text;

/**
 * Factory for all processes implementing {@link NivaProcess}.
 * <p>These will be named "niva:&lt;class-name&gt;".</p>
 * 
 * @author Roar Brænden
 *
 */
public class NivaProcessFactory extends AnnotatedBeanProcessFactory {

    static volatile BeanFactoryRegistry<NivaProcess> registry;

    public static BeanFactoryRegistry<NivaProcess> getRegistry() {
        if (registry == null) {
            synchronized (NivaProcessFactory.class) {
                if (registry == null) {
                    registry = new BeanFactoryRegistry<NivaProcess>(NivaProcess.class);
                }
            }
        }
        return registry;
    }

    public NivaProcessFactory() {
        super(Text.text("Niva processes"), "niva", getRegistry().lookupBeanClasses());
    }

}
