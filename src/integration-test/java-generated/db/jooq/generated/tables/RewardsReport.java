/*
 * This file is generated by jOOQ.
 */
package db.jooq.generated.tables;


import db.jooq.generated.Public;
import db.jooq.generated.tables.records.RewardsReportRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.Function10;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Records;
import org.jooq.Row10;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RewardsReport extends TableImpl<RewardsReportRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.rewards_report</code>
     */
    public static final RewardsReport REWARDS_REPORT = new RewardsReport();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RewardsReportRecord> getRecordType() {
        return RewardsReportRecord.class;
    }

    /**
     * The column <code>public.rewards_report.customer_id</code>.
     */
    public final TableField<RewardsReportRecord, Integer> CUSTOMER_ID = createField(DSL.name("customer_id"), SQLDataType.INTEGER.nullable(false).identity(true).defaultValue(DSL.field("nextval('customer_customer_id_seq'::regclass)", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>public.rewards_report.store_id</code>.
     */
    public final TableField<RewardsReportRecord, Integer> STORE_ID = createField(DSL.name("store_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.rewards_report.first_name</code>.
     */
    public final TableField<RewardsReportRecord, String> FIRST_NAME = createField(DSL.name("first_name"), SQLDataType.VARCHAR(45).nullable(false), this, "");

    /**
     * The column <code>public.rewards_report.last_name</code>.
     */
    public final TableField<RewardsReportRecord, String> LAST_NAME = createField(DSL.name("last_name"), SQLDataType.VARCHAR(45).nullable(false), this, "");

    /**
     * The column <code>public.rewards_report.email</code>.
     */
    public final TableField<RewardsReportRecord, String> EMAIL = createField(DSL.name("email"), SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.rewards_report.address_id</code>.
     */
    public final TableField<RewardsReportRecord, Integer> ADDRESS_ID = createField(DSL.name("address_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.rewards_report.activebool</code>.
     */
    public final TableField<RewardsReportRecord, Boolean> ACTIVEBOOL = createField(DSL.name("activebool"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("true", SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.rewards_report.create_date</code>.
     */
    public final TableField<RewardsReportRecord, LocalDate> CREATE_DATE = createField(DSL.name("create_date"), SQLDataType.LOCALDATE.nullable(false).defaultValue(DSL.field("('now'::text)::date", SQLDataType.LOCALDATE)), this, "");

    /**
     * The column <code>public.rewards_report.last_update</code>.
     */
    public final TableField<RewardsReportRecord, LocalDateTime> LAST_UPDATE = createField(DSL.name("last_update"), SQLDataType.LOCALDATETIME(6).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.rewards_report.active</code>.
     */
    public final TableField<RewardsReportRecord, Integer> ACTIVE = createField(DSL.name("active"), SQLDataType.INTEGER, this, "");

    private RewardsReport(Name alias, Table<RewardsReportRecord> aliased) {
        this(alias, aliased, new Field[] {
            DSL.val(null, SQLDataType.INTEGER),
            DSL.val(null, SQLDataType.NUMERIC)
        });
    }

    private RewardsReport(Name alias, Table<RewardsReportRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.function());
    }

    /**
     * Create an aliased <code>public.rewards_report</code> table reference
     */
    public RewardsReport(String alias) {
        this(DSL.name(alias), REWARDS_REPORT);
    }

    /**
     * Create an aliased <code>public.rewards_report</code> table reference
     */
    public RewardsReport(Name alias) {
        this(alias, REWARDS_REPORT);
    }

    /**
     * Create a <code>public.rewards_report</code> table reference
     */
    public RewardsReport() {
        this(DSL.name("rewards_report"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<RewardsReportRecord, Integer> getIdentity() {
        return (Identity<RewardsReportRecord, Integer>) super.getIdentity();
    }

    @Override
    public RewardsReport as(String alias) {
        return new RewardsReport(DSL.name(alias), this, parameters);
    }

    @Override
    public RewardsReport as(Name alias) {
        return new RewardsReport(alias, this, parameters);
    }

    @Override
    public RewardsReport as(Table<?> alias) {
        return new RewardsReport(alias.getQualifiedName(), this, parameters);
    }

    /**
     * Rename this table
     */
    @Override
    public RewardsReport rename(String name) {
        return new RewardsReport(DSL.name(name), null, parameters);
    }

    /**
     * Rename this table
     */
    @Override
    public RewardsReport rename(Name name) {
        return new RewardsReport(name, null, parameters);
    }

    /**
     * Rename this table
     */
    @Override
    public RewardsReport rename(Table<?> name) {
        return new RewardsReport(name.getQualifiedName(), null, parameters);
    }

    // -------------------------------------------------------------------------
    // Row10 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row10<Integer, Integer, String, String, String, Integer, Boolean, LocalDate, LocalDateTime, Integer> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    /**
     * Call this table-valued function
     */
    public RewardsReport call(
          Integer minMonthlyPurchases
        , BigDecimal minDollarAmountPurchased
    ) {
        RewardsReport result = new RewardsReport(DSL.name("rewards_report"), null, new Field[] {
            DSL.val(minMonthlyPurchases, SQLDataType.INTEGER),
            DSL.val(minDollarAmountPurchased, SQLDataType.NUMERIC)
        });

        return aliased() ? result.as(getUnqualifiedName()) : result;
    }

    /**
     * Call this table-valued function
     */
    public RewardsReport call(
          Field<Integer> minMonthlyPurchases
        , Field<BigDecimal> minDollarAmountPurchased
    ) {
        RewardsReport result = new RewardsReport(DSL.name("rewards_report"), null, new Field[] {
            minMonthlyPurchases,
            minDollarAmountPurchased
        });

        return aliased() ? result.as(getUnqualifiedName()) : result;
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function10<? super Integer, ? super Integer, ? super String, ? super String, ? super String, ? super Integer, ? super Boolean, ? super LocalDate, ? super LocalDateTime, ? super Integer, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function10<? super Integer, ? super Integer, ? super String, ? super String, ? super String, ? super Integer, ? super Boolean, ? super LocalDate, ? super LocalDateTime, ? super Integer, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
