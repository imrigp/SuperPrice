package database;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import server.entities.ItemPrice;

public interface ItemPriceDao {
    @NotNull
    List<ItemPrice> getItemsPrice(long chainId, int storeId, List<Long> itemIds);
}
