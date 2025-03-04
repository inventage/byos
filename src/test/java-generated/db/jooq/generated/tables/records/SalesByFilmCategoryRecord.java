/*
 * This file is generated by jOOQ.
 */
package db.jooq.generated.tables.records;


import db.jooq.generated.tables.SalesByFilmCategory;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SalesByFilmCategoryRecord extends TableRecordImpl<SalesByFilmCategoryRecord> implements Record2<String, BigDecimal> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.sales_by_film_category.category</code>.
     */
    public void setCategory(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.sales_by_film_category.category</code>.
     */
    public String getCategory() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.sales_by_film_category.total_sales</code>.
     */
    public void setTotalSales(BigDecimal value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.sales_by_film_category.total_sales</code>.
     */
    public BigDecimal getTotalSales() {
        return (BigDecimal) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, BigDecimal> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, BigDecimal> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return SalesByFilmCategory.SALES_BY_FILM_CATEGORY.CATEGORY;
    }

    @Override
    public Field<BigDecimal> field2() {
        return SalesByFilmCategory.SALES_BY_FILM_CATEGORY.TOTAL_SALES;
    }

    @Override
    public String component1() {
        return getCategory();
    }

    @Override
    public BigDecimal component2() {
        return getTotalSales();
    }

    @Override
    public String value1() {
        return getCategory();
    }

    @Override
    public BigDecimal value2() {
        return getTotalSales();
    }

    @Override
    public SalesByFilmCategoryRecord value1(String value) {
        setCategory(value);
        return this;
    }

    @Override
    public SalesByFilmCategoryRecord value2(BigDecimal value) {
        setTotalSales(value);
        return this;
    }

    @Override
    public SalesByFilmCategoryRecord values(String value1, BigDecimal value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SalesByFilmCategoryRecord
     */
    public SalesByFilmCategoryRecord() {
        super(SalesByFilmCategory.SALES_BY_FILM_CATEGORY);
    }

    /**
     * Create a detached, initialised SalesByFilmCategoryRecord
     */
    public SalesByFilmCategoryRecord(String category, BigDecimal totalSales) {
        super(SalesByFilmCategory.SALES_BY_FILM_CATEGORY);

        setCategory(category);
        setTotalSales(totalSales);
    }
}
