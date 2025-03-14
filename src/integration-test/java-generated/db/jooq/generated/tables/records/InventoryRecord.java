/*
 * This file is generated by jOOQ.
 */
package db.jooq.generated.tables.records;


import db.jooq.generated.tables.Inventory;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InventoryRecord extends UpdatableRecordImpl<InventoryRecord> implements Record4<Integer, Integer, Integer, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.inventory.inventory_id</code>.
     */
    public void setInventoryId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.inventory.inventory_id</code>.
     */
    public Integer getInventoryId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.inventory.film_id</code>.
     */
    public void setFilmId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.inventory.film_id</code>.
     */
    public Integer getFilmId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.inventory.store_id</code>.
     */
    public void setStoreId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.inventory.store_id</code>.
     */
    public Integer getStoreId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>public.inventory.last_update</code>.
     */
    public void setLastUpdate(LocalDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.inventory.last_update</code>.
     */
    public LocalDateTime getLastUpdate() {
        return (LocalDateTime) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, Integer, Integer, LocalDateTime> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Integer, Integer, Integer, LocalDateTime> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return Inventory.INVENTORY.INVENTORY_ID;
    }

    @Override
    public Field<Integer> field2() {
        return Inventory.INVENTORY.FILM_ID;
    }

    @Override
    public Field<Integer> field3() {
        return Inventory.INVENTORY.STORE_ID;
    }

    @Override
    public Field<LocalDateTime> field4() {
        return Inventory.INVENTORY.LAST_UPDATE;
    }

    @Override
    public Integer component1() {
        return getInventoryId();
    }

    @Override
    public Integer component2() {
        return getFilmId();
    }

    @Override
    public Integer component3() {
        return getStoreId();
    }

    @Override
    public LocalDateTime component4() {
        return getLastUpdate();
    }

    @Override
    public Integer value1() {
        return getInventoryId();
    }

    @Override
    public Integer value2() {
        return getFilmId();
    }

    @Override
    public Integer value3() {
        return getStoreId();
    }

    @Override
    public LocalDateTime value4() {
        return getLastUpdate();
    }

    @Override
    public InventoryRecord value1(Integer value) {
        setInventoryId(value);
        return this;
    }

    @Override
    public InventoryRecord value2(Integer value) {
        setFilmId(value);
        return this;
    }

    @Override
    public InventoryRecord value3(Integer value) {
        setStoreId(value);
        return this;
    }

    @Override
    public InventoryRecord value4(LocalDateTime value) {
        setLastUpdate(value);
        return this;
    }

    @Override
    public InventoryRecord values(Integer value1, Integer value2, Integer value3, LocalDateTime value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached InventoryRecord
     */
    public InventoryRecord() {
        super(Inventory.INVENTORY);
    }

    /**
     * Create a detached, initialised InventoryRecord
     */
    public InventoryRecord(Integer inventoryId, Integer filmId, Integer storeId, LocalDateTime lastUpdate) {
        super(Inventory.INVENTORY);

        setInventoryId(inventoryId);
        setFilmId(filmId);
        setStoreId(storeId);
        setLastUpdate(lastUpdate);
    }
}
