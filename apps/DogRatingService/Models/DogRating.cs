namespace RatingCommentsService.Models;

public class DogRating
{
    public long Id { get; set; }
    public long DogId { get; set; }
    public int Value { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}