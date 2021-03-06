/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.result;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentSelector;
import org.gradle.api.artifacts.result.ComponentSelectionReason;
import org.gradle.api.internal.artifacts.ModuleVersionIdentifierSerializer;
import org.gradle.internal.resolve.ModuleVersionResolveException;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.Encoder;

import java.io.IOException;
import java.util.Map;

public class InternalDependencyResultSerializer {
    private final static byte SUCCESSFUL = 0;
    private final static byte FAILED = 1;
    private final ComponentSelectorSerializer componentSelectorSerializer = new ComponentSelectorSerializer();
    private final ComponentSelectionReasonSerializer componentSelectionReasonSerializer = new ComponentSelectionReasonSerializer();
    private final ModuleVersionIdentifierSerializer moduleVersionIdentifierSerializer = new ModuleVersionIdentifierSerializer();

    public InternalDependencyResult read(Decoder decoder, Map<ComponentSelector, ModuleVersionResolveException> failures) throws IOException {
        ComponentSelector requested = componentSelectorSerializer.read(decoder);
        byte resultByte = decoder.readByte();
        if (resultByte == SUCCESSFUL) {
            ModuleVersionIdentifier selected = moduleVersionIdentifierSerializer.read(decoder);
            return new DefaultInternalDependencyResult(requested, selected, null, null);
        } else if (resultByte == FAILED) {
            ComponentSelectionReason reason = componentSelectionReasonSerializer.read(decoder);
            ModuleVersionResolveException failure = failures.get(requested);
            return new DefaultInternalDependencyResult(requested, null, reason, failure);
        } else {
            throw new IllegalArgumentException("Unknown result byte: " + resultByte);
        }
    }

    public void write(Encoder encoder, InternalDependencyResult value) throws IOException {
        componentSelectorSerializer.write(encoder, value.getRequested());
        if (value.getFailure() == null) {
            encoder.writeByte(SUCCESSFUL);
            moduleVersionIdentifierSerializer.write(encoder, value.getSelected());
        } else {
            encoder.writeByte(FAILED);
            componentSelectionReasonSerializer.write(encoder, value.getReason());
        }
    }
}
