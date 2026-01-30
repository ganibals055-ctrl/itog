namespace RatingCommentsService.Models;

public class DogComment
{
    public long Id { get; set; }
    public long DogId { get; set; }
    public string Text { get; set; } = "";
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}