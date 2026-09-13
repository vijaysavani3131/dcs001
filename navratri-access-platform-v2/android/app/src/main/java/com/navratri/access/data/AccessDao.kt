package com.navratri.access.data

import androidx.room.*

@Dao
interface AccessDao {
    @Query("SELECT * FROM event_config WHERE id=1") suspend fun config():EventConfigEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putConfig(v:EventConfigEntity)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putDays(v:List<EventDayEntity>)
    @Query("DELETE FROM event_days") suspend fun clearDays()

    @Query("SELECT * FROM tickets WHERE ticketCode=:code") suspend fun ticket(code:String):TicketEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putTicket(v:TicketEntity)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putTickets(v:List<TicketEntity>)
    @Query("UPDATE tickets SET status=:status WHERE ticketCode=:code") suspend fun setTicketStatus(code:String,status:String)

    @Query("SELECT * FROM ticket_days WHERE ticketCode=:code AND dayNumber=:day") suspend fun ticketDay(code:String,day:Int):TicketDayEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putTicketDay(v:TicketDayEntity)
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun putTicketDays(v:List<TicketDayEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM scan_events WHERE id=:id)") suspend fun scanExists(id:String):Boolean
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun putScan(v:ScanEventEntity)
    @Query("SELECT * FROM scan_events WHERE syncState='PENDING' ORDER BY occurredAt LIMIT :limit") suspend fun pending(limit:Int=1000):List<ScanEventEntity>
    @Query("UPDATE scan_events SET syncState='SYNCED' WHERE id IN (:ids)") suspend fun markSynced(ids:List<String>)
    @Query("SELECT COALESCE(SUM(d.entries * t.peopleCount),0) FROM ticket_days d JOIN tickets t ON t.ticketCode=d.ticketCode WHERE d.dayNumber=:day") suspend fun entriesToday(day:Int):Int
    @Query("SELECT COALESCE(SUM(t.peopleCount),0) FROM ticket_days d JOIN tickets t ON t.ticketCode=d.ticketCode WHERE d.dayNumber=:day AND d.state='INSIDE'") suspend fun insideNow(day:Int):Int
    @Query("SELECT COUNT(*) FROM scan_events WHERE syncState='PENDING'") suspend fun pendingCount():Int
}
