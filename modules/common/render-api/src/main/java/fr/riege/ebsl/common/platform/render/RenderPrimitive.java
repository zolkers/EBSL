/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.platform.render;

/**
 * Represents a renderable world primitive.
 *
 * <p>Primitives know how to draw themselves through immediate render handles or retained world-render sessions while resolving fallback styles.</p>
 */
public sealed interface RenderPrimitive permits RenderPrimitive.Line, RenderPrimitive.FilledBox, RenderPrimitive.WireBox {
    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param handle the immediate render handle
     * @param fallbackStyle the style used when this primitive does not define one
     */
    void render(RenderHandle handle, RenderStyle fallbackStyle);

    /**
     * Renders this component for the active frame using the supplied runtime context.
 *
     * @param session the retained render session
     * @param fallbackStyle the style used when this primitive does not define one
     */
    void render(WorldRenderSession session, RenderStyle fallbackStyle);

    /**
     * Returns the style that should be used for rendering after applying the fallback.
 *
     * @param fallbackStyle the style used when this primitive does not define one
     * @return the value defined by this contract
     */
    RenderStyle effectiveStyle(RenderStyle fallbackStyle);

    record Line(double x1, double y1, double z1,
                double x2, double y2, double z2,
                RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .lineWidth(s.lineWidth())
                .ignoreDepth(s.ignoreDepth())
                .line(x1, y1, z1, x2, y2, z2);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .lineWidth(s.lineWidth())
                .line(x1, y1, z1, x2, y2, z2);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    record FilledBox(double minX, double minY, double minZ,
                     double maxX, double maxY, double maxZ,
                     RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .ignoreDepth(s.ignoreDepth())
                .filledBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .filledBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    record WireBox(double minX, double minY, double minZ,
                   double maxX, double maxY, double maxZ,
                   RenderStyle style) implements RenderPrimitive {
        @Override
        public void render(RenderHandle handle, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            WorldRender.builder(handle)
                .paint(s.paint())
                .lineWidth(s.lineWidth())
                .ignoreDepth(s.ignoreDepth())
                .wireBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public void render(WorldRenderSession session, RenderStyle fallbackStyle) {
            RenderStyle s = effectiveStyle(fallbackStyle);
            session.paint(s.paint())
                .lineWidth(s.lineWidth())
                .wireBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public RenderStyle effectiveStyle(RenderStyle fallbackStyle) {
            return styleOrDefault(style, fallbackStyle);
        }
    }

    private static RenderStyle styleOrDefault(RenderStyle style, RenderStyle fallbackStyle) {
        if (style != null) {
            return style;
        }
        return fallbackStyle != null ? fallbackStyle : RenderStyle.DEFAULT;
    }
}
