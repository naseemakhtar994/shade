/*
 * Copyright (c) 2016 Tatsuya Maki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.t28.shade.converter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DefaultConverter implements Converter<Void, Void> {
    @NonNull
    @Override
    public Void toConverted(@Nullable Void supported) {
        return supported;
    }

    @NonNull
    @Override
    public Void toSupported(@Nullable Void converted) {
        return converted;
    }
}