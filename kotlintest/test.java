package com.ebay.posttxn.cancel.type;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\000,\n\002\030\002\n\002\030\002\n\000\n\002\030\002\n\002\b\006\n\002\020\013\n\000\n\002\020\000\n\000\n\002\020\b\n\000\n\002\020\016\n\002\b\002\b\b\030\0002\0020\001:\001\021B\017\022\b\b\002\020\002\032\0020\003\006\002\020\004J\t\020\007\032\0020\003H\003J\023\020\b\032\0020\0002\b\b\002\020\002\032\0020\003H\001J\023\020\t\032\0020\n2\b\020\013\032\004\030\0010\fH\003J\t\020\r\032\0020\016H\001J\t\020\017\032\0020\020H\001R\021\020\002\032\0020\003\006\b\n\000\032\004\b\005\020\006\006\022"}, d2 = {"Lcom/ebay/posttxn/cancel/type/CancelResponse;", "Lcom/ebay/posttxn/cancel/type/BaseResponse;", "modules", "Lcom/ebay/posttxn/cancel/type/CancelResponse$Modules;", "(Lcom/ebay/posttxn/cancel/type/CancelResponse$Modules;)V", "getModules", "()Lcom/ebay/posttxn/cancel/type/CancelResponse$Modules;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "Modules", "cancelexpsvc"})
public final class CancelResponse extends BaseResponse {
  @NotNull
  private final Modules modules;

  public CancelResponse(@NotNull Modules modules) { this.modules = modules; } @NotNull public final Modules getModules() { return this.modules; } public CancelResponse() { this(null, 1, null); } @NotNull public final Modules component1() { return this.modules; } @NotNull public final CancelResponse copy(@NotNull Modules modules) { Intrinsics.checkParameterIsNotNull(modules, "modules"); return new CancelResponse(modules); } @NotNull public String toString() { return "CancelResponse(modules=" + this.modules + ")"; } public int hashCode() { return (this.modules != null) ? this.modules.hashCode() : 0; } public boolean equals(@Nullable Object paramObject) { if (this != paramObject) { if (paramObject instanceof CancelResponse) { CancelResponse cancelResponse = (CancelResponse)paramObject; if (Intrinsics.areEqual(this.modules, cancelResponse.modules))
    return true;  }  } else { return true; }  return false; } @JsonInclude(JsonInclude.Include.NON_NULL) @Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\000V\n\002\030\002\n\002\020\000\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\000\n\002\030\002\n\002\b0\n\002\020\013\n\002\b\002\n\002\020\b\n\000\n\002\020\016\n\000\b\b\030\0002\0020\001Bq\022\n\b\002\020\002\032\004\030\0010\003\022\n\b\002\020\004\032\004\030\0010\005\022\n\b\002\020\006\032\004\030\0010\007\022\n\b\002\020\b\032\004\030\0010\t\022\n\b\002\020\n\032\004\030\0010\013\022\n\b\002\020\f\032\004\030\0010\r\022\n\b\002\020\016\032\004\030\0010\017\022\n\b\002\020\020\032\004\030\0010\021\022\n\b\002\020\022\032\004\030\0010\023\006\002\020\024J\013\0209\032\004\030\0010\003H\003J\013\020:\032\004\030\0010\005H\003J\013\020;\032\004\030\0010\007H\003J\013\020<\032\004\030\0010\tH\003J\013\020=\032\004\030\0010\013H\003J\013\020>\032\004\030\0010\rH\003J\013\020?\032\004\030\0010\017H\003J\013\020@\032\004\030\0010\021H\003J\013\020A\032\004\030\0010\023H\003Ju\020B\032\0020\0002\n\b\002\020\002\032\004\030\0010\0032\n\b\002\020\004\032\004\030\0010\0052\n\b\002\020\006\032\004\030\0010\0072\n\b\002\020\b\032\004\030\0010\t2\n\b\002\020\n\032\004\030\0010\0132\n\b\002\020\f\032\004\030\0010\r2\n\b\002\020\016\032\004\030\0010\0172\n\b\002\020\020\032\004\030\0010\0212\n\b\002\020\022\032\004\030\0010\023H\001J\023\020C\032\0020D2\b\020E\032\004\030\0010\001H\003J\t\020F\032\0020GH\001J\t\020H\032\0020IH\001R\034\020\016\032\004\030\0010\017X\016\006\016\n\000\032\004\b\025\020\026\"\004\b\027\020\030R\034\020\002\032\004\030\0010\003X\016\006\016\n\000\032\004\b\031\020\032\"\004\b\033\020\034R\034\020\006\032\004\030\0010\007X\016\006\016\n\000\032\004\b\035\020\036\"\004\b\037\020 R\034\020\022\032\004\030\0010\023X\016\006\016\n\000\032\004\b!\020\"\"\004\b#\020$R\034\020\004\032\004\030\0010\005X\016\006\016\n\000\032\004\b%\020&\"\004\b'\020(R\034\020\n\032\004\030\0010\013X\016\006\016\n\000\032\004\b)\020*\"\004\b+\020,R\034\020\f\032\004\030\0010\rX\016\006\016\n\000\032\004\b-\020.\"\004\b/\0200R\034\020\020\032\004\030\0010\021X\016\006\016\n\000\032\004\b1\0202\"\004\b3\0204R\034\020\b\032\004\030\0010\tX\016\006\016\n\000\032\004\b5\0206\"\004\b7\0208\006J"}, d2 = {"Lcom/ebay/posttxn/cancel/type/CancelResponse$Modules;", "", "breadcrumbModule", "Lcom/ebay/posttxn/cancel/module/BreadcrumbModule;", "headerModule", "Lcom/ebay/posttxn/cancel/module/HeaderModule;", "cancelBodyModule", "Lcom/ebay/posttxn/cancel/module/CancelBodyModule;", "summaryModule", "Lcom/ebay/posttxn/cancel/module/SummaryModule;", "reasonModule", "Lcom/ebay/posttxn/cancel/module/ReasonModule;", "refundSummaryModule", "Lcom/ebay/posttxn/cancel/module/RefundSummaryModule;", "actionModule", "Lcom/ebay/posttxn/cancel/module/ActionModule;", "statusModule", "Lcom/ebay/posttxn/cancel/module/StatusModule;", "errorModule", "Lcom/ebay/posttxn/cancel/module/ErrorModule;", "(Lcom/ebay/posttxn/cancel/module/BreadcrumbModule;Lcom/ebay/posttxn/cancel/module/HeaderModule;Lcom/ebay/posttxn/cancel/module/CancelBodyModule;Lcom/ebay/posttxn/cancel/module/SummaryModule;Lcom/ebay/posttxn/cancel/module/ReasonModule;Lcom/ebay/posttxn/cancel/module/RefundSummaryModule;Lcom/ebay/posttxn/cancel/module/ActionModule;Lcom/ebay/posttxn/cancel/module/StatusModule;Lcom/ebay/posttxn/cancel/module/ErrorModule;)V", "getActionModule", "()Lcom/ebay/posttxn/cancel/module/ActionModule;", "setActionModule", "(Lcom/ebay/posttxn/cancel/module/ActionModule;)V", "getBreadcrumbModule", "()Lcom/ebay/posttxn/cancel/module/BreadcrumbModule;", "setBreadcrumbModule", "(Lcom/ebay/posttxn/cancel/module/BreadcrumbModule;)V", "getCancelBodyModule", "()Lcom/ebay/posttxn/cancel/module/CancelBodyModule;", "setCancelBodyModule", "(Lcom/ebay/posttxn/cancel/module/CancelBodyModule;)V", "getErrorModule", "()Lcom/ebay/posttxn/cancel/module/ErrorModule;", "setErrorModule", "(Lcom/ebay/posttxn/cancel/module/ErrorModule;)V", "getHeaderModule", "()Lcom/ebay/posttxn/cancel/module/HeaderModule;", "setHeaderModule", "(Lcom/ebay/posttxn/cancel/module/HeaderModule;)V", "getReasonModule", "()Lcom/ebay/posttxn/cancel/module/ReasonModule;", "setReasonModule", "(Lcom/ebay/posttxn/cancel/module/ReasonModule;)V", "getRefundSummaryModule", "()Lcom/ebay/posttxn/cancel/module/RefundSummaryModule;", "setRefundSummaryModule", "(Lcom/ebay/posttxn/cancel/module/RefundSummaryModule;)V", "getStatusModule", "()Lcom/ebay/posttxn/cancel/module/StatusModule;", "setStatusModule", "(Lcom/ebay/posttxn/cancel/module/StatusModule;)V", "getSummaryModule", "()Lcom/ebay/posttxn/cancel/module/SummaryModule;", "setSummaryModule", "(Lcom/ebay/posttxn/cancel/module/SummaryModule;)V", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "", "cancelexpsvc"}) public static final class Modules
  {
    @Nullable private BreadcrumbModule breadcrumbModule; @Nullable private HeaderModule headerModule; @Nullable private CancelBodyModule cancelBodyModule; @Nullable private SummaryModule summaryModule; public Modules(@Nullable BreadcrumbModule breadcrumbModule, @Nullable HeaderModule headerModule, @Nullable CancelBodyModule cancelBodyModule, @Nullable SummaryModule summaryModule, @Nullable ReasonModule reasonModule, @Nullable RefundSummaryModule refundSummaryModule, @Nullable ActionModule actionModule, @Nullable StatusModule statusModule, @Nullable ErrorModule errorModule) { this.breadcrumbModule = breadcrumbModule; this.headerModule = headerModule; this.cancelBodyModule = cancelBodyModule; this.summaryModule = summaryModule; this.reasonModule = reasonModule; this.refundSummaryModule = refundSummaryModule; this.actionModule = actionModule; this.statusModule = statusModule; this.errorModule = errorModule; } @Nullable private ReasonModule reasonModule; @Nullable private RefundSummaryModule refundSummaryModule; @Nullable private ActionModule actionModule; @Nullable private StatusModule statusModule; @Nullable private ErrorModule errorModule; @Nullable
  public final BreadcrumbModule getBreadcrumbModule() { return this.breadcrumbModule; } public final void setBreadcrumbModule(@Nullable BreadcrumbModule <set-?>) { this.breadcrumbModule = <set-?>; } @Nullable
  public final HeaderModule getHeaderModule() { return this.headerModule; } public final void setHeaderModule(@Nullable HeaderModule <set-?>) { this.headerModule = <set-?>; } @Nullable
  public final CancelBodyModule getCancelBodyModule() { return this.cancelBodyModule; } public final void setCancelBodyModule(@Nullable CancelBodyModule <set-?>) { this.cancelBodyModule = <set-?>; } @Nullable
  public final SummaryModule getSummaryModule() { return this.summaryModule; } public final void setSummaryModule(@Nullable SummaryModule <set-?>) { this.summaryModule = <set-?>; } @Nullable
  public final ReasonModule getReasonModule() { return this.reasonModule; } public final void setReasonModule(@Nullable ReasonModule <set-?>) { this.reasonModule = <set-?>; } @Nullable
  public final RefundSummaryModule getRefundSummaryModule() { return this.refundSummaryModule; } public final void setRefundSummaryModule(@Nullable RefundSummaryModule <set-?>) { this.refundSummaryModule = <set-?>; } @Nullable
  public final ActionModule getActionModule() { return this.actionModule; } public final void setActionModule(@Nullable ActionModule <set-?>) { this.actionModule = <set-?>; } @Nullable
  public final StatusModule getStatusModule() { return this.statusModule; } public final void setStatusModule(@Nullable StatusModule <set-?>) { this.statusModule = <set-?>; } @Nullable
  public final ErrorModule getErrorModule() { return this.errorModule; } public final void setErrorModule(@Nullable ErrorModule <set-?>) { this.errorModule = <set-?>; }

    public Modules() { this(null, null, null, null, null, null, null, null, null, 511, null); }

    @Nullable
    public final BreadcrumbModule component1() { return this.breadcrumbModule; }

    @Nullable
    public final HeaderModule component2() { return this.headerModule; }

    @Nullable
    public final CancelBodyModule component3() { return this.cancelBodyModule; }

    @Nullable
    public final SummaryModule component4() { return this.summaryModule; }

    @Nullable
    public final ReasonModule component5() { return this.reasonModule; }

    @Nullable
    public final RefundSummaryModule component6() { return this.refundSummaryModule; }

    @Nullable
    public final ActionModule component7() { return this.actionModule; }

    @Nullable
    public final StatusModule component8() { return this.statusModule; }

    @Nullable
    public final ErrorModule component9() { return this.errorModule; }

    @NotNull
    public final Modules copy(@Nullable BreadcrumbModule breadcrumbModule, @Nullable HeaderModule headerModule, @Nullable CancelBodyModule cancelBodyModule, @Nullable SummaryModule summaryModule, @Nullable ReasonModule reasonModule, @Nullable RefundSummaryModule refundSummaryModule, @Nullable ActionModule actionModule, @Nullable StatusModule statusModule, @Nullable ErrorModule errorModule) { return new Modules(breadcrumbModule, headerModule, cancelBodyModule, summaryModule, reasonModule, refundSummaryModule, actionModule, statusModule, errorModule); }

    @NotNull
    public String toString() { return "Modules(breadcrumbModule=" + this.breadcrumbModule + ", headerModule=" + this.headerModule + ", cancelBodyModule=" + this.cancelBodyModule + ", summaryModule=" + this.summaryModule + ", reasonModule=" + this.reasonModule + ", refundSummaryModule=" + this.refundSummaryModule + ", actionModule=" + this.actionModule + ", statusModule=" + this.statusModule + ", errorModule=" + this.errorModule + ")"; }

    public int hashCode() { return (((((((((this.breadcrumbModule != null) ? this.breadcrumbModule.hashCode() : 0) * 31 + ((this.headerModule != null) ? this.headerModule.hashCode() : 0)) * 31 + ((this.cancelBodyModule != null) ? this.cancelBodyModule.hashCode() : 0)) * 31 + ((this.summaryModule != null) ? this.summaryModule.hashCode() : 0)) * 31 + ((this.reasonModule != null) ? this.reasonModule.hashCode() : 0)) * 31 + ((this.refundSummaryModule != null) ? this.refundSummaryModule.hashCode() : 0)) * 31 + ((this.actionModule != null) ? this.actionModule.hashCode() : 0)) * 31 + ((this.statusModule != null) ? this.statusModule.hashCode() : 0)) * 31 + ((this.errorModule != null) ? this.errorModule.hashCode() : 0); }

    public boolean equals(@Nullable Object param1Object) {
      if (this != param1Object) {
        if (param1Object instanceof Modules) {
          Modules modules = (Modules)param1Object;
          if (Intrinsics.areEqual(this.breadcrumbModule, modules.breadcrumbModule) && Intrinsics.areEqual(this.headerModule, modules.headerModule) && Intrinsics.areEqual(this.cancelBodyModule, modules.cancelBodyModule) && Intrinsics.areEqual(this.summaryModule, modules.summaryModule) && Intrinsics.areEqual(this.reasonModule, modules.reasonModule) && Intrinsics.areEqual(this.refundSummaryModule, modules.refundSummaryModule) && Intrinsics.areEqual(this.actionModule, modules.actionModule) && Intrinsics.areEqual(this.statusModule, modules.statusModule) && Intrinsics.areEqual(this.errorModule, modules.errorModule))
            return true;
        }
      } else {
        return true;
      }
      return false;
    }
  }
}