/**
 * Copyright (C) 2013-2014 Kametic <epo.jemba@kametic.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * or any later version
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.nuun.plugin.rest;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.KernelParamsRequest;
import io.nuun.kernel.core.AbstractPlugin;
import io.nuun.plugin.web.NuunWebPlugin;

import java.util.Collection;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.kametic.specifications.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.Scopes;

public class NuunRestPlugin extends AbstractPlugin
{

    Logger logger = LoggerFactory.getLogger(NuunRestPlugin.class);
    
    public static String NUUN_REST_URL_PATTERN                   = "nuun.rest.url.pattern";
    public static String NUUN_REST_PACKAGE_ROOT                  = "nuun.rest.package.root";
    public static String NUUN_JERSEY_GUICECONTAINER_CUSTOM_CLASS = "nuun.jersey.guicecontainer.custom.class";
    public static String NUUN_REST_POJO_MAPPING_FEATURE_ENABLED  = "nuun.rest.pojo.mapping.feature.enabled";

    private boolean      enablePojoMappingFeature                = true;
    private Class<? extends HttpServlet>     jerseyCustomClass   = null;

    private String       urlPattern;

    private Module       module;

    @Override
    public String name()
    {
        return "nuun-rest-plugin";
    }

    @Override
    public Object nativeUnitModule()
    {

        if (module == null)
        {

            module = new NuunRestModule(urlPattern, enablePojoMappingFeature , jerseyCustomClass);
        }

        return module;
    }

    @SuppressWarnings("unchecked")
	@Override
    public InitState init(InitContext initContext)
    {
        
        String urlPatternFromKernel = initContext.kernelParam(NUUN_REST_URL_PATTERN);
        
        if ( urlPatternFromKernel != null && ! urlPatternFromKernel.trim().equals(""))
        {
            urlPattern = urlPatternFromKernel;
        }
        
        if (urlPattern == null || urlPattern.trim().equals(""))
        {
            throw new PluginException( NUUN_REST_URL_PATTERN + " can not be null for plugin " + this.getClass().getName() + ".");
        }
        
        String pojo = initContext.kernelParam(NUUN_REST_POJO_MAPPING_FEATURE_ENABLED);
        if (pojo != null && !pojo.isEmpty())
        {
            enablePojoMappingFeature = Boolean.valueOf(pojo);
        }
        String strJerseyClass = initContext.kernelParam(NUUN_JERSEY_GUICECONTAINER_CUSTOM_CLASS);
        if (strJerseyClass != null && !strJerseyClass.isEmpty() )
        {
        	try {
				if (jerseyCustomClass != null )
				{
				    logger.info( String.format("Overringing %s by %s via kernel parameter %s." , jerseyCustomClass.getName() , strJerseyClass, NUUN_JERSEY_GUICECONTAINER_CUSTOM_CLASS));
				}
        	    jerseyCustomClass = (Class<? extends HttpServlet>) Class.forName(strJerseyClass);
			} catch (ClassNotFoundException e) {
				throw new PluginException ( strJerseyClass + " does not exists as class.", e);
			}
        }
        
        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<BindingRequest> bindingRequests()
    {
        
        Specification<Class<?>> specificationForNoScope = or (
                classAnnotatedWith(Path.class) ,
                classMethodsAnnotatedWith(Path.class)
   		);
        Specification<Class<?>> specificationForSingletonScop =  or(
                and( classAnnotatedWith(Provider.class) , classImplements(MessageBodyWriter.class)) ,
                and( classAnnotatedWith(Provider.class) , classImplements(ContextResolver.class)) ,
                and( classAnnotatedWith(Provider.class) , classImplements(MessageBodyReader.class)) ,
                and( classAnnotatedWith(Provider.class) , classImplements(ExceptionMapper.class))
               ) ;
                
        return bindingRequestsBuilder()
                .specification(specificationForNoScope)
                .specification(specificationForSingletonScop).withScope(Scopes.SINGLETON)
                .build();
    }

    /*
     * (non-Javadoc)
     * @see com.inetpsa.nuun.core.plugin.AbstractStsPlugin#kernelParamsRequired()
     */

    @Override
    public Collection<KernelParamsRequest> kernelParamsRequests()
    {
        return kernelParamsRequestBuilder() //
        		.mandatory(NUUN_REST_URL_PATTERN) //
        		.optional(NUUN_REST_POJO_MAPPING_FEATURE_ENABLED).build();
    }

    /*
     * (non-Javadoc)
     * @see com.inetpsa.nuun.core.plugin.AbstractStsPlugin#pluginsRequired()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins()
    {
        return collectionOf(NuunWebPlugin.class);
    }
    
    public void setjerseyCustomClass(Class<? extends HttpServlet> klass)
    {
        jerseyCustomClass = klass;
    }
    
    public void setUrlPattern(String urlPattern)
    {
        this.urlPattern = urlPattern;
    }

}
