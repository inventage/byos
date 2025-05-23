/*
 * This file is generated by jOOQ.
 */
package db.jooq.generated.tables;


import db.jooq.generated.Indexes;
import db.jooq.generated.Keys;
import db.jooq.generated.Public;
import db.jooq.generated.tables.records.FilmActorRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function3;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FilmActor extends TableImpl<FilmActorRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.film_actor</code>
     */
    public static final FilmActor FILM_ACTOR = new FilmActor();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<FilmActorRecord> getRecordType() {
        return FilmActorRecord.class;
    }

    /**
     * The column <code>public.film_actor.actor_id</code>.
     */
    public final TableField<FilmActorRecord, Integer> ACTOR_ID = createField(DSL.name("actor_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.film_actor.film_id</code>.
     */
    public final TableField<FilmActorRecord, Integer> FILM_ID = createField(DSL.name("film_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.film_actor.last_update</code>.
     */
    public final TableField<FilmActorRecord, LocalDateTime> LAST_UPDATE = createField(DSL.name("last_update"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    private FilmActor(Name alias, Table<FilmActorRecord> aliased) {
        this(alias, aliased, null);
    }

    private FilmActor(Name alias, Table<FilmActorRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.film_actor</code> table reference
     */
    public FilmActor(String alias) {
        this(DSL.name(alias), FILM_ACTOR);
    }

    /**
     * Create an aliased <code>public.film_actor</code> table reference
     */
    public FilmActor(Name alias) {
        this(alias, FILM_ACTOR);
    }

    /**
     * Create a <code>public.film_actor</code> table reference
     */
    public FilmActor() {
        this(DSL.name("film_actor"), null);
    }

    public <O extends Record> FilmActor(Table<O> child, ForeignKey<O, FilmActorRecord> key) {
        super(child, key, FILM_ACTOR);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.IDX_FK_FILM_ID);
    }

    @Override
    public UniqueKey<FilmActorRecord> getPrimaryKey() {
        return Keys.FILM_ACTOR_PKEY;
    }

    @Override
    public List<ForeignKey<FilmActorRecord, ?>> getReferences() {
        return Arrays.asList(Keys.FILM_ACTOR__FILM_ACTOR_ACTOR_ID_FKEY, Keys.FILM_ACTOR__FILM_ACTOR_FILM_ID_FKEY);
    }

    private transient Actor _actor;
    private transient Film _film;

    /**
     * Get the implicit join path to the <code>public.actor</code> table.
     */
    public Actor actor() {
        if (_actor == null)
            _actor = new Actor(this, Keys.FILM_ACTOR__FILM_ACTOR_ACTOR_ID_FKEY);

        return _actor;
    }

    /**
     * Get the implicit join path to the <code>public.film</code> table.
     */
    public Film film() {
        if (_film == null)
            _film = new Film(this, Keys.FILM_ACTOR__FILM_ACTOR_FILM_ID_FKEY);

        return _film;
    }

    @Override
    public FilmActor as(String alias) {
        return new FilmActor(DSL.name(alias), this);
    }

    @Override
    public FilmActor as(Name alias) {
        return new FilmActor(alias, this);
    }

    @Override
    public FilmActor as(Table<?> alias) {
        return new FilmActor(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public FilmActor rename(String name) {
        return new FilmActor(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public FilmActor rename(Name name) {
        return new FilmActor(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public FilmActor rename(Table<?> name) {
        return new FilmActor(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, LocalDateTime> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function3<? super Integer, ? super Integer, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function3<? super Integer, ? super Integer, ? super LocalDateTime, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
