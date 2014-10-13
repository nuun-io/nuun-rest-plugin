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

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;
import static org.fest.assertions.Assertions.assertThat;
import io.nuun.kernel.api.Kernel;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;

public class NuunRestPluginTest
{

    static Kernel underTest;
	
	@BeforeClass
    public static void init()
    {
        
        underTest = createKernel(
                //
                newKernelConfiguration() //
                .params( //
                        Kernel.NUUN_ROOT_PACKAGE , "org.nuunframework", //
                        NuunRestPlugin.NUUN_JERSEY_GUICECONTAINER_CUSTOM_CLASS , SampleGuiceContainer.class.getName() , //
                        NuunRestPlugin.NUUN_REST_URL_PATTERN , "/rest/*" ) //
                );

        
        
        underTest.init();
        underTest.start();
    }
    
    @Test
	public void custom_guicecontainer_should_be_usable ()
	{
		assertThat(
		   underTest.objectGraph().as(Injector.class).getInstance(SampleGuiceContainer.class)
		).isNotNull();
		assertThat(SampleGuiceContainer.initialized).isTrue();
	}

	
    @AfterClass
    public static void stop()
    {
    	underTest.stop();
    }
    
    void subresources_should_be_bound ()
    {
        
    }
}
