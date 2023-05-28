/*
 *    Copyright (c) 2022-2023 Tim Langhammer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.jpangolin.jtzipi.mymod.fx.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class ServicePathSearch extends Service<List<Path>> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( "SearchPathService" );

    private final ObjectProperty<Path> fxRootPathPropFX = new SimpleObjectProperty<>( this, "FX_ROOT_PATH_PROP" );
    private final ObjectProperty<Predicate<? super Path>> fxPathPredicateProp = new SimpleObjectProperty<>( this, "FX_PATH_PREDICATE_PROP" );

    private ServicePathSearch() {

        this.fxRootPathPropFX.addListener( this::onRootChange );
        this.fxPathPredicateProp.addListener( this::onPredicateChange );
    }

    public void setRootPath( final Path rootDir ) {

        Objects.requireNonNull( rootDir );
        if ( !Files.isDirectory( rootDir ) ) {
            throw new IllegalArgumentException( "rootDir[='" + rootDir + "'] seems to be no dir" );
        }

        fxRootPathPropFX.setValue( rootDir );
    }

    public void setPathPredicate( final Predicate<? super Path> pp ) {

        Objects.requireNonNull( pp );
        fxPathPredicateProp.setValue( pp );
    }

    public ObjectProperty<Path> fxRootDirProp() {

        return fxRootPathPropFX;
    }

    public ObjectProperty<Predicate<? super Path>> fxPathPredicateProp() {

        return fxPathPredicateProp;
    }

    private void onRootChange( final ObservableValue<? extends Path> obs, Path pathOld, Path pathNew ) {


    }

    private void onPredicateChange( final ObservableValue<? extends Predicate<? super Path>> obs, Predicate<? super Path> pOld, Predicate<? super Path> pNew ) {

    }


    @Override
    protected Task<List<Path>> createTask() {

        Path root = fxRootPathPropFX.getValue();
        Predicate<? super Path> pp = fxPathPredicateProp.getValue();


        return SearchPathTask.of( root, pp );
    }
}
