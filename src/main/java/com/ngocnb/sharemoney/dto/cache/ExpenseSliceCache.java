package com.ngocnb.sharemoney.dto.cache;


import com.ngocnb.sharemoney.dto.response.ExpenseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSliceCache {

    private List<ExpenseResponse> content;

    private boolean hasNext;
}