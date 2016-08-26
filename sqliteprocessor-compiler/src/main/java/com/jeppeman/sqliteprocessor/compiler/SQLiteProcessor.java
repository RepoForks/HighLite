package com.jeppeman.sqliteprocessor.compiler;

import com.google.auto.service.AutoService;
import com.jeppeman.sqliteprocessor.AutoIncrement;
import com.jeppeman.sqliteprocessor.PrimaryKey;
import com.jeppeman.sqliteprocessor.SQLiteDatabaseHelper;
import com.jeppeman.sqliteprocessor.SQLiteField;
import com.jeppeman.sqliteprocessor.SQLiteGetter;
import com.jeppeman.sqliteprocessor.SQLiteSetter;
import com.jeppeman.sqliteprocessor.SQLiteTable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by jesper on 2016-08-25.
 */
@AutoService(Processor.class)
public class SQLiteProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Types mTypeUtils;
    private Filer mFiler;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        mElementUtils = processingEnv.getElementUtils();
        mTypeUtils = processingEnv.getTypeUtils();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {

        for (final Element element
                : roundEnv.getElementsAnnotatedWith(SQLiteDatabaseHelper.class)) {
            final SQLiteDatabaseHelper dbAnno = element.getAnnotation(SQLiteDatabaseHelper.class);

            final JavaFile helperFile = new SQLiteProcessorHelperClass(dbAnno.name(),
                    getTableElementMappingForHelper(roundEnv, element),
                    dbAnno.version(), mElementUtils).writeJava();

            try {
                helperFile.writeTo(mFiler);
            } catch (IOException e) {
                error(element, "Unable to write helper file for %s: %s",
                        element.asType().toString(), e.getMessage());
            }
        }

        for (final Element element : roundEnv.getElementsAnnotatedWith(SQLiteTable.class)) {
            final SQLiteTable tableAnno = element.getAnnotation(SQLiteTable.class);

            TypeMirror mirror = null;
            try {
                tableAnno.sqLiteHelper();
            } catch (MirroredTypeException e) {
                mirror = e.getTypeMirror();
            }

            final JavaFile daoFile = new SQLiteDAOClass(ClassName.bestGuess(mirror.toString()),
                    tableAnno,
                    element, mElementUtils).writeJava();

            try {
                daoFile.writeTo(mFiler);
            } catch (IOException e) {
                error(element, "Unable to write helper file for %s: %s",
                        element.asType().toString(), e.getMessage());
            }
        }


        return true;
    }

    private Map<SQLiteTable, Element> getTableElementMappingForHelper(
            final RoundEnvironment roundEnvironment,
            final Element helperElement) {
        final Map<SQLiteTable, Element> ret = new LinkedHashMap<>();

        for (final Element element : roundEnvironment.getElementsAnnotatedWith(SQLiteTable.class)) {
            final SQLiteTable tableAnno = element.getAnnotation(SQLiteTable.class);

            TypeMirror mirror = null;
            try {
                tableAnno.sqLiteHelper();
            } catch (MirroredTypeException e) {
                mirror = e.getTypeMirror();
            }
            if (mTypeUtils.isSameType(mirror, helperElement.asType())) {
                ret.put(tableAnno, element);
            }
        }

        return ret;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> types = new LinkedHashSet<>();
        for (final Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        final Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(AutoIncrement.class);
        annotations.add(PrimaryKey.class);
        annotations.add(SQLiteField.class);
        annotations.add(SQLiteGetter.class);
        annotations.add(SQLiteSetter.class);
        annotations.add(SQLiteTable.class);
        annotations.add(SQLiteDatabaseHelper.class);

        return annotations;
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
}