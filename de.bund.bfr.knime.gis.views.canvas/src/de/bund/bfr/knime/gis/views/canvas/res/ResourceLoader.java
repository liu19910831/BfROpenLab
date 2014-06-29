/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.gis.views.canvas.res;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.osgi.framework.FrameworkUtil;

public class ResourceLoader {

	private static ResourceLoader instance = null;

	private Image bfrLogo;

	private ResourceLoader() throws IOException {
		bfrLogo = ImageIO.read(FrameworkUtil.getBundle(ResourceLoader.class)
				.getResource(
						"de/bund/bfr/knime/gis/views/canvas/res/BfR-Logo.png"));
	}

	public static ResourceLoader getInstance() {
		if (instance == null) {
			try {
				instance = new ResourceLoader();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return instance;
	}

	public Image getBfrLogo() {
		return bfrLogo;
	}
}
